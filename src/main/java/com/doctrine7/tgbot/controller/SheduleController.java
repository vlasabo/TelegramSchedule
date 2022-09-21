package com.doctrine7.tgbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(value = "updateShedule/")
public class SheduleController {

	@PostMapping
	public void update(@RequestParam String employee, @RequestParam String patient,
					   @RequestParam String procedure, @RequestParam String time) {

	}

}
