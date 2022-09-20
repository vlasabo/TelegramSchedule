package com.doctrine7.TGbot.service;

import com.doctrine7.TGbot.model.Shedule;
import com.doctrine7.TGbot.model.User;

import java.util.ArrayList;
import java.util.List;

public class SheduleService {
	private final List<Shedule> allShedule;

	public SheduleService(List<Shedule> allShedule) {
		this.allShedule = allShedule;
	}

	public List<Shedule> actualizeByEmployee(User user) {
		List<Shedule> actual = new ArrayList<>();
		for (Shedule shedule : allShedule) {
			if (user.getEmployees().contains(shedule.getEmployee())) {
				actual.add(shedule);
			}
		}
		return actual;
	}
}
