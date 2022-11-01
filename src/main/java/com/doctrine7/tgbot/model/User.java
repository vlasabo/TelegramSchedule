package com.doctrine7.tgbot.model;

import com.google.common.collect.Streams;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Data
@Entity(name = "usersDataTable")

public class User {

	@Id
	private Long chatId;

	private String firstName;
	private String lastName;
	private String userName;
	private Timestamp registeredAt;
	private int registrationAttempts;

	public void addEmployee(String employeeName, EmployeeRepository employeeRepository) {
		Employee emp = new Employee();
		emp.setUser_id(this.chatId);
		emp.setEmployee(employeeName);
		employeeRepository.save(emp);
	}

	public void deleteEmployee(int nom, EmployeeRepository employeeRepository) {
		var allEmp = employeeRepository.findAll();
		var setOfEmployeesFromBd =
				Streams.stream(allEmp).distinct().filter(x -> Objects.equals(x.getUser_id(), this.chatId))
						.collect(Collectors.toList());
		if ((nom <= setOfEmployeesFromBd.size()) && (nom > 0)) {
			employeeRepository.delete(setOfEmployeesFromBd.get(nom - 1));
		}
	}

	public String allEmployeesToMessage(EmployeeRepository employeeRepository) {
		StringBuilder sb = new StringBuilder();
		int i = 1;
		var allEmp = employeeRepository.findAll();
		var setOfEmployeesFromBd =
				Streams.stream(allEmp).distinct().filter(x -> Objects.equals(x.getUser_id(), this.chatId))
						.map(Employee::getEmployee).collect(Collectors.toList());
		for (String s : setOfEmployeesFromBd) {
			sb.append(i).append(": ").append(s).append(", \n");
			i++;
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 3);
		}
		return sb.toString();
	}

	public Set<String> getEmployees(EmployeeRepository employeeRepository) {
		return Streams.stream(employeeRepository.findAll()).distinct().filter(x -> Objects.equals(x.getUser_id(), this.chatId))
				.map(Employee::getEmployee).collect(Collectors.toSet());
	}
}
