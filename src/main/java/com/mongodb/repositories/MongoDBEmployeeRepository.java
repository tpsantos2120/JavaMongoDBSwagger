package com.mongodb.repositories;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.controllers.EmployeeController;
import com.mongodb.dtos.AverageAge;
import com.mongodb.models.Employee;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static java.util.Arrays.asList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Repository
public class MongoDBEmployeeRepository implements EmployeeRepository {

    private static final TransactionOptions txnOptions = TransactionOptions.builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.MAJORITY)
            .writeConcern(WriteConcern.MAJORITY)
            .build();
    private final MongoClient client;
    private MongoCollection<Employee> employeeCollection;

    public MongoDBEmployeeRepository(MongoClient mongoClient) {
        this.client = mongoClient;
    }

    @PostConstruct
    void init() {
        System.out.println(client);
        employeeCollection = client.getDatabase("employee_db").getCollection("employees", Employee.class);
    }

    @Override
    public Employee save(Employee employee) {
        employee.setId(new ObjectId());
        employeeCollection.insertOne(employee);
        return employee;
    }

    @Override
    public List<Employee> saveAll(List<Employee> employees) {
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(() -> {
                employees.forEach(p -> p.setId(new ObjectId()));
                employeeCollection.insertMany(clientSession, employees);
                return employees;
            }, txnOptions);
        }
    }

    @Override
    public List<Employee> findAll() {
        ArrayList<Employee> employees = employeeCollection.find().into(new ArrayList<>());
        employees.forEach(e -> e.add(linkTo(methodOn(EmployeeController.class).getEmployee(String.valueOf(e.getId()))).withSelfRel()));
        employees.forEach(e -> e.add(linkTo(methodOn(EmployeeController.class).getEmployees()).withRel("employees")));
        return employees;
    }

    @Override
    public List<Employee> findAll(List<String> ids) {
        ArrayList<Employee> employees = employeeCollection.find(in("_id", mapToObjectIds(ids))).into(new ArrayList<>());
        employees.forEach(e -> e.add(linkTo(methodOn(EmployeeController.class).getEmployee(String.valueOf(e.getId()))).withSelfRel()));
        employees.forEach(e -> e.add(linkTo(methodOn(EmployeeController.class).getEmployees()).withRel("employees")));
        return employees;
    }

    @Override
    public Employee findOne(String id) {
        Employee employee =  employeeCollection.find(eq("_id", new ObjectId(id))).first();
        assert employee != null;
        employee.add(linkTo(methodOn(EmployeeController.class).getEmployees()).withRel("employees"));
        return employee;
    }

    @Override
    public long count() {
        return employeeCollection.countDocuments();
    }

    @Override
    public long delete(String id) {
        return employeeCollection.deleteOne(eq("_id", new ObjectId(id))).getDeletedCount();
    }

    @Override
    public long delete(List<String> ids) {
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(
                    () -> employeeCollection.deleteMany(clientSession, in("_id", mapToObjectIds(ids))).getDeletedCount(),
                    txnOptions);
        }
    }

    @Override
    public long deleteAll() {
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(
                    () -> employeeCollection.deleteMany(clientSession, new BsonDocument()).getDeletedCount(), txnOptions);
        }
    }

    @Override
    public Employee update(Employee employee) {
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().returnDocument(AFTER);
        return employeeCollection.findOneAndReplace(eq("_id", employee.getId()), employee, options);
    }

    @Override
    public long update(List<Employee> employees) {
        List<WriteModel<Employee>> writes = employees.stream()
                .map(p -> new ReplaceOneModel<>(eq("_id", p.getId()), p))
                .collect(Collectors.toList());
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(
                    () -> employeeCollection.bulkWrite(clientSession, writes).getModifiedCount(), txnOptions);
        }
    }

    @Override
    public double getAverageAge() {
        List<Bson> pipeline = asList(group(new BsonNull(), avg("averageAge", "$age")), project(excludeId()));
        return employeeCollection.aggregate(pipeline, AverageAge.class).first().getAverageAge();
    }

    private List<ObjectId> mapToObjectIds(List<String> ids) {
        return ids.stream().map(ObjectId::new).collect(Collectors.toList());
    }
}
