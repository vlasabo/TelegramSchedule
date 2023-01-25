package com.doctrine7.tgbot.service;

import com.doctrine7.tgbot.model.AppointmentsList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentsListService {
	private final TelegramBot telegramBot;
	private final String NEW_DOCUMENT = "Создан новый лист назначений № ";
	private final String OLD_DOCUMENT = "Отредактирован существующий лист назначений № ";

	public void prepareNotifications(AppointmentsList document, long groupId) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		LocalDateTime documentDateTime = LocalDateTime.parse(document.getDate(), dtf);


		var changeFlag = documentDateTime.plusMinutes(5).isBefore(LocalDateTime.now()); //пять минут врачу на изменения
		if (changeFlag) {
			sendChangeNotify(document, groupId);
			return;
		}
		sendCreateNotify(document, groupId);
	}

	private void sendCreateNotify(AppointmentsList document, long groupId) {
		StringBuilder messageText = new StringBuilder(NEW_DOCUMENT);
		messageText.append(document.getId())
				.append(" на пациента ")
				.append(document.getPatient())
				.append(". Врачи: \n")
				.append("невролог: ")
				.append("".equals(document.getNeurologist()) ? "-" : document.getNeurologist()).append("\n")
				.append("реабилитолог: ")
				.append("".equals(document.getRehabilitologist()) ? "-" : document.getRehabilitologist()).append("\n")
				.append("логопед: ")
				.append("".equals(document.getSpeechTherapist()) ? "-" : document.getSpeechTherapist());

		try {
			telegramBot.sendMessageToId(groupId, messageText.toString());
		} catch (TelegramApiException e) {
			log.error("!cant send message to group, " + e.getMessage());
		}
	}

	private void sendChangeNotify(AppointmentsList document, long groupId) {
		StringBuilder messageText = new StringBuilder(OLD_DOCUMENT);
		messageText.append(document.getId())
				.append(" на пациента ")
				.append(document.getPatient());

		try {
			telegramBot.sendMessageToId(groupId, messageText.toString());
		} catch (
				TelegramApiException e) {
			log.error("!cant send message to group, " + e.getMessage());
		}
	}
}
