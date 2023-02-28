package com.doctrine7.tgbot.service;

import com.doctrine7.tgbot.model.Employee;
import com.doctrine7.tgbot.model.EmployeeRepository;
import com.doctrine7.tgbot.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public Set<Employee> getEmployees(long userId) {
        return employeeRepository.findAllByUserIdIs(userId);
    }

    public Set<String> getEmployeesNames(long userId) {
        return employeeRepository.findAllByUserIdIs(userId).stream()
                .map(Employee::getEmployee)
                .collect(Collectors.toSet());
    }

    public List<Long> findAllByEmployeeIn(List<String> allEmployeesWhoNeedMessage) {
        return employeeRepository.findAllByEmployeeIn(allEmployeesWhoNeedMessage);
    }
}
