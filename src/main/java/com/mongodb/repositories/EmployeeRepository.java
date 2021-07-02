package com.mongodb.repositories;

import com.mongodb.models.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository {

    Employee save(Employee person);

    List<Employee> saveAll(List<Employee> persons);

    List<Employee> findAll();

    List<Employee> findAll(List<String> ids);

    Employee findOne(String id);

    long count();

    long delete(String id);

    long delete(List<String> ids);

    long deleteAll();

    Employee update(Employee person);

    long update(List<Employee> persons);

    double getAverageAge();

}
