package com.doctrine7.tgbot.controller;

import com.doctrine7.tgbot.model.UserRepository;
import com.doctrine7.tgbot.service.SheduleUpdateMessageSender;
import com.doctrine7.tgbot.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RestController
@Slf4j
public class SheduleController {

	@Autowired
	private TelegramBot telegramBot;
	@Autowired
	private UserRepository userRepository;


	@PostMapping
	@RequestMapping(value = "updateShedule/")
	public void change(@RequestParam(defaultValue = "") String lastEmployee,
					   @RequestParam(defaultValue = "") String lastPatient,
					   @RequestParam(defaultValue = "") String lastProcedure,
					   @RequestParam(defaultValue = "") String lastTime,
					   @RequestParam String employee, @RequestParam String patient,
					   @RequestParam String procedure, @RequestParam String time) throws TelegramApiException {
		new SheduleUpdateMessageSender(lastEmployee, lastProcedure, lastPatient,
				lastTime, employee, procedure, patient, time).sendSheduleUpdate(telegramBot, userRepository);
	}


}
