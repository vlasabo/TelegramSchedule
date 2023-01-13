package com.doctrine7.tgbot.service;

import com.doctrine7.tgbot.model.EmployeeRepository;
import com.doctrine7.tgbot.model.Shedule;
import com.doctrine7.tgbot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SheduleService {
	private final List<Shedule> allShedule;
	@Autowired
	private final EmployeeRepository employeeRepository;

	public SheduleService(List<Shedule> allShedule, EmployeeRepository employeeRepository) {
		this.allShedule = allShedule;
		this.employeeRepository = employeeRepository;
	}

	public List<Shedule> actualizeByEmployee(User user) {
		List<Shedule> actual = new ArrayList<>();
		var userEmployeesList = user.getEmployees(employeeRepository);
		if (user.getSeparatedShedule() == null || !user.getSeparatedShedule()) {
			for (Shedule shedule : allShedule) {
				if (userEmployeesList.contains(shedule.getEmployee())) {
					actual.add(shedule);
				}
			}
		} else {
			for (String employee : userEmployeesList) {
				for (Shedule shedule : allShedule) {
					if (Objects.equals(shedule.getEmployee(), employee)) {
						actual.add(shedule);
					}
				}
			}
		}
		log.info("will get a schedule for {}", userEmployeesList);
		return actual;
	}
}
