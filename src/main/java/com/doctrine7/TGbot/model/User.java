package com.doctrine7.TGbot.model;

import lombok.Data;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity(name = "usersDataTable")

public class User {

	@Id
	private Long chatId;

	private String firstName;
	private String lastName;
	private String userName;
	private String employee;
	private Timestamp registeredAt;
	private int registrationAttempts;
	private boolean registrationPassed;

}
