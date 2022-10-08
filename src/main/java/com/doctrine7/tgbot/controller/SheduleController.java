package com.doctrine7.tgbot.controller;

import com.doctrine7.tgbot.service.SheduleUpdateSender;
import com.doctrine7.tgbot.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class SheduleController {

	@Autowired
	private TelegramBot telegramBot;

	@PostMapping
	@RequestMapping(value = "updateShedule/")
	public void update(@RequestParam String employee, @RequestParam String patient,
					   @RequestParam String procedure, @RequestParam String time) {
		System.out.println(employee.concat(patient).concat(procedure).concat(time));
	}

	@PostMapping
	@RequestMapping(value = "changeShedule/")
	public void change(@RequestParam(defaultValue = "") String lastEmployee,
					   @RequestParam(defaultValue = "") String lastPatient,
					   @RequestParam(defaultValue = "") String lastProcedure,
					   @RequestParam(defaultValue = "") String lastTime,
					   @RequestParam String employee, @RequestParam String patient,
					   @RequestParam String procedure, @RequestParam String time) {
		System.out.println(employee.concat(patient).concat(procedure).concat(time));
		SheduleUpdateSender sheduleUpdateSender = new SheduleUpdateSender(lastEmployee, lastProcedure, lastPatient,
				lastTime, employee, procedure, patient, time);
		var mapResultEmployeeMessage = sheduleUpdateSender.getMessagesWhatsHappening();
		for (Map.Entry<String, String> entry : mapResultEmployeeMessage.entrySet()) {
			//TODO: достать всех юзеров, сравнить у кого находится employee из мапы, достать ид и отправить сообщение
		}
	}

}
