package com.doctrine7.tgbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentsList {
	private String id;
	private String patient;
	private String date;
	private String neurologist;
	private String speechTherapist;
	private String rehabilitologist;
	private String editor;
	private Boolean isNew;
}
