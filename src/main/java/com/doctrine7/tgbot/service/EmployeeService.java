package com.doctrine7.tgbot.service;

import com.doctrine7.tgbot.model.Employee;
import com.doctrine7.tgbot.model.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Set<String> getEmployeesNames(long userId) {
        return employeeRepository.findAllByUserIdIs(userId).stream()
                .map(Employee::getName)
                .collect(Collectors.toSet());
    }

    public Map<Long, Set<String>> getEmployeesNamesForListUsers(List<Long> usersIds) {
        Map<Long, Set<String>> result = new HashMap<>();
        usersIds.forEach(id -> result.put(id, getEmployeesNames(id)));
        return result;
    }


    public List<Long> findAllByEmployeeIn(List<String> allEmployeesWhoNeedMessage) {
        return employeeRepository.findAllByNameIn(allEmployeesWhoNeedMessage);
    }

    public void addEmployee(long userId, String employeeName) {
        Employee employee = new Employee();
        employee.setUserId(userId);
        employee.setName(employeeName);
        employeeRepository.save(employee);
    }

    public void deleteEmployee(int nom, long chatId) {
        var listOfEmployeesFromBd =
                employeeRepository.findAllByUserIdIsOrderByName(chatId).stream()
                        .distinct()
                        .collect(Collectors.toList());
        if ((nom <= listOfEmployeesFromBd.size()) && (nom > 0)) {
            log.warn("delete employee {} from user {}", listOfEmployeesFromBd.get(nom - 1), chatId);
            employeeRepository.delete(listOfEmployeesFromBd.get(nom - 1));
        }
    }

    public String allEmployeesToMessage(long chatId) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        var setOfEmployeesFromBd = employeeRepository.findAllByUserIdIsOrderByName(chatId).stream()
                .distinct()
                .map(Employee::getName)
                .collect(Collectors.toList());
        for (String s : setOfEmployeesFromBd) {
            sb.append(i).append(": ").append(s).append(", \n");
            i++;
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 3);
        }
        return sb.toString();
    }
}
