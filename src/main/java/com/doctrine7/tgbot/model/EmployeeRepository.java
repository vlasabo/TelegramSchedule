package com.doctrine7.tgbot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    List<Employee> findAllByUserIdIs(long userId);

    List<Long> findAllByEmployeeIn(List<String> employees);
}
