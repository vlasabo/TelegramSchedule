package com.doctrine7.tgbot.controller;

import com.doctrine7.tgbot.model.AppointmentsList;
import com.doctrine7.tgbot.service.AppointmentsListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
public class DocumentUpdateController {
	private final AppointmentsListService appointmentsListService;

	@PostMapping
	@RequestMapping(value = "appointment/")
	public void change(@RequestParam Long id,
					   @RequestParam Long groupId,
					   @RequestParam(defaultValue = "") String neurologist,
					   @RequestParam(defaultValue = "") String speechTherapist,
					   @RequestParam(defaultValue = "") String rehabilitologist,
					   @RequestParam(defaultValue = "") String date,
					   @RequestParam(defaultValue = "") String patient) {
		AppointmentsList document =
				new AppointmentsList(id, patient, date, neurologist, speechTherapist, rehabilitologist);
		appointmentsListService.prepareNotifications(document, groupId);
	}
}
