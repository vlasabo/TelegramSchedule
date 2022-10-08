package com.doctrine7.tgbot.controller;

import com.doctrine7.tgbot.model.User;
import com.doctrine7.tgbot.model.UserRepository;
import com.doctrine7.tgbot.service.SheduleUpdateMessageCreater;
import com.doctrine7.tgbot.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

@RestController
@Slf4j
public class SheduleController {
	@Autowired
	private TelegramBot telegramBot;
	@Autowired
	private UserRepository userRepository;

	@PostMapping
	@RequestMapping(value = "changeShedule/")
	public void update(@RequestParam String employee, @RequestParam String patient,
					   @RequestParam String procedure, @RequestParam String time) {

	}

	@PostMapping
	@RequestMapping(value = "updateShedule/")
	public void change(@RequestParam(defaultValue = "") String lastEmployee,
					   @RequestParam(defaultValue = "") String lastPatient,
					   @RequestParam(defaultValue = "") String lastProcedure,
					   @RequestParam(defaultValue = "") String lastTime,
					   @RequestParam String employee, @RequestParam String patient,
					   @RequestParam String procedure, @RequestParam String time) throws TelegramApiException {
		SheduleUpdateMessageCreater sheduleUpdateMessageCreater = new SheduleUpdateMessageCreater(lastEmployee, lastProcedure, lastPatient,
				lastTime, employee, procedure, patient, time);
		var mapResultEmployeeMessage = sheduleUpdateMessageCreater.getMessagesWhatsHappening();
		var allUsersIterable = userRepository.findAll();

		for (Map.Entry<String, String> entry : mapResultEmployeeMessage.entrySet()) {
			for (User user : allUsersIterable) {
				if (user.getEmployees().contains(entry.getKey())) {
					telegramBot.sendMessageToId(user.getChatId(), entry.getValue());
					log.info("sending a message about changes in the schedule to the employee {}", entry.getKey());
				}
			}
		}
	}

}
