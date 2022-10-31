package com.doctrine7.tgbot.model;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity(name = "users_employees")
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Long id;

	private String employee;

	private Long user_id;
}
