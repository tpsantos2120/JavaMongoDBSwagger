package com.mongodb.controllers;

import com.mongodb.models.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.repositories.EmployeeRepository;

import java.util.List;

import static java.util.Arrays.asList;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("employee")
    @ResponseStatus(HttpStatus.CREATED)
    public Employee postEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee);
    }

    @PostMapping("employees")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Employee> postEmployees(@RequestBody List<Employee> employees) {
        return employeeRepository.saveAll(employees);
    }

    @GetMapping("employees")
    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("employee/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String id) {
        Employee employee = employeeRepository.findOne(id);
        if (employee == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(employee);
    }

    @GetMapping("employees/{ids}")
    public List<Employee> getEmployees(@PathVariable String ids) {
        List<String> listIds = asList(ids.split(","));
        return employeeRepository.findAll(listIds);
    }

    @GetMapping("employees/count")
    public Long getCount() {
        return employeeRepository.count();
    }

    @DeleteMapping("employee/{id}")
    public Long deleteEmployee(@PathVariable String id) {
        return employeeRepository.delete(id);
    }

    @DeleteMapping("employees/{ids}")
    public Long deleteEmployees(@PathVariable String ids) {
        List<String> listIds = asList(ids.split(","));
        return employeeRepository.delete(listIds);
    }

    @DeleteMapping("employees")
    public Long deleteEmployees() {
        return employeeRepository.deleteAll();
    }

    @PutMapping("employee")
    public Employee putEmployee(@RequestBody Employee employee) {
        return employeeRepository.update(employee);
    }

    @PutMapping("employees")
    public Long putEmployee(@RequestBody List<Employee> persons) {
        return employeeRepository.update(persons);
    }

    @GetMapping("employees/average/age")
    public Double averageAge() {
        return employeeRepository.getAverageAge();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final Exception handleAllExceptions(RuntimeException e) {
        LOGGER.error("Internal server error.", e);
        return e;
    }
}
