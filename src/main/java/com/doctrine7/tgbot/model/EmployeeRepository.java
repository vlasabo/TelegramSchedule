package com.doctrine7.tgbot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    Set<Employee> findAllByUserIdIs(long userId);

    List<Long> findAllByEmployeeIn(List<String> employees);
}
