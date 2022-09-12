package com.doctrine7.TGbot.service;

import com.doctrine7.TGbot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

	final BotConfig config;

	public TelegramBot(BotConfig config) {
		this.config = config;
	}

	@Override
	public String getBotUsername() {
		return config.getBotName();
	}

	@Override
	public String getBotToken() {
		return config.getToken();
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String text = update.getMessage().getText(); //command
			long chatId = update.getMessage().getChatId();

			switch (text) {
				case "/start":
					try {
						startCommand(chatId);
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
					break;

				default:
					try {
						sendMessageToId(chatId, "Command not found!");
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
			}

		}
	}

	private void startCommand(long chatId) throws TelegramApiException {
		String answerText = "test from id=" + chatId;
		sendMessageToId(chatId, answerText);
	}

	private void sendMessageToId(long chatId, String textToSend) throws TelegramApiException {
		SendMessage outputMessage = new SendMessage();
		outputMessage.setChatId(chatId);
		outputMessage.setText(textToSend);

		try {
			execute(outputMessage);
		} catch (TelegramApiException e) {
			log.error(e.getMessage());
		}
	}
}
