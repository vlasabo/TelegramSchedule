package com.doctrine7.tgbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.ArrayList;

@Data
@Entity(name = "usersDataTable")

public class User {

	@Id
	private Long chatId;

	private String firstName;
	private String lastName;
	private String userName;
	//TODO: тут должен быть сет, иначе кто-нибудь дважды зарегистрируется
	private ArrayList<String> employees = new ArrayList<>();

	private Timestamp registeredAt;
	private int registrationAttempts;

	public void addEmployee(String employee) {
		this.employees.add(employee);
	}

	public String allEmployeesToMessage() {
		StringBuilder sb = new StringBuilder();
		for (String s : employees) {
			sb.append(s).append(", ");
		}
		sb.deleteCharAt(sb.length() - 2);
		return sb.toString();
	}
}
