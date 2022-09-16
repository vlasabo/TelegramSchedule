package com.doctrine7.TGbot.model;

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
	private ArrayList<String> employees=new ArrayList<>();
	private Timestamp registeredAt;
	private int registrationAttempts;
	private boolean registrationPassed;

	public void addEmployee(String employee){
		this.employees.add(employee);
	}
}
