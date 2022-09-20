package com.doctrine7.TGbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Shedule {

	private String time;
	private String employee;
	private String patient;
	private String procedure;

	@Override
	public String toString() {
		return time + " - " + patient + ", " + procedure + "\n";
	}
}
