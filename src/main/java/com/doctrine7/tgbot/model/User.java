package com.doctrine7.tgbot.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.stream.Collectors;


@Getter
@Setter
@Entity(name = "usersDataTable")
@Slf4j
public class User {

    @Id
    private Long chatId;

    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registeredAt;
    private int registrationAttempts;
    private Boolean separatedShedule;
    //TODO: это переписать. Всю работу с сотрудниками унести в сервис

    public void addEmployee(String employeeName, EmployeeRepository employeeRepository) {
        Employee emp = new Employee();
        emp.setUserId(this.chatId);
        emp.setEmployee(employeeName);
        employeeRepository.save(emp);
    }

    public void deleteEmployee(int nom, EmployeeRepository employeeRepository) {
        var setOfEmployeesFromBd =
                employeeRepository.findAllByUserIdIs(chatId).stream()
                        .distinct()
                        .collect(Collectors.toList());
        if ((nom <= setOfEmployeesFromBd.size()) && (nom > 0)) {
            log.warn("delete employee {} from user {}", setOfEmployeesFromBd.get(nom - 1), this.userName);
            employeeRepository.delete(setOfEmployeesFromBd.get(nom - 1));
        }
    }

    public String allEmployeesToMessage(EmployeeRepository employeeRepository) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        var setOfEmployeesFromBd = employeeRepository.findAllByUserIdIs(chatId).stream()
                .distinct()
                .map(Employee::getEmployee)
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
