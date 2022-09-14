package com.doctrine7.TGbot.service;

import com.doctrine7.TGbot.config.BotConfig;
import com.doctrine7.TGbot.config.ResponseToSqlConfig;
import com.doctrine7.TGbot.model.SQLDatabaseConnection;
import com.doctrine7.TGbot.model.User;
import com.doctrine7.TGbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

	final BotConfig config;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ResponseToSqlConfig configSql;

	public TelegramBot(BotConfig config) {
		this.config = config;
		List<BotCommand> listOfCommands = new ArrayList<>();
		listOfCommands.add(new BotCommand("/start","Registration"));
		listOfCommands.add(new BotCommand("/today","today"));
		listOfCommands.add(new BotCommand("/tomorrow","tomorrow"));
		try{
			execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(),null));
		} catch (TelegramApiException e){
			log.error(e.getMessage());
		}
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
						regCommand(chatId);
						registerUser(update.getMessage());
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
					break;
				case "/today":
					try {
						scheduleToday(chatId);
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
					break;
					case "/tomorrow":
					try {
						scheduleTomorrow(chatId);
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
					break;

				default:
					try {
						sendMessageToId(chatId, "Command not found!");
						log.debug("unrecognized command "+text + " from user @" + update.getMessage().getChat().getUserName());
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
			}

		}
	}

	private void registerUser(Message message) {
		if (userRepository.findById(message.getChatId()).isEmpty()){
			Long chatId=message.getChatId();
			Chat chat = message.getChat();

			User user = new User();

			user.setChatId(chatId);
			user.setFirstName(chat.getFirstName());
			user.setLastName(chat.getLastName());
			user.setUserName(chat.getUserName());
			user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

			userRepository.save(user);
			log.info("new user " + user);
		} else {
			log.error("Попытка повторной регистрации пользователя "+message.getChat().getUserName());
		}
	}

	private void regCommand(long chatId) throws TelegramApiException {
		String answerText = "reg from id=" + chatId;
		sendMessageToId(chatId, answerText);
	}

	private void scheduleToday(long chatId) throws TelegramApiException {
		String answerText = "расписание на сегодня для id=" + chatId+"\n";
		SQLDatabaseConnection request = new SQLDatabaseConnection(configSql);
		request.sendRequest(LocalDate.now());
		if (request.getResponse().length()>4000){
			answerText = answerText + "\n" + request.getResponse().substring(0,4000);
		} else {
			answerText = answerText + request.getResponse();
		}
		sendMessageToId(chatId, answerText);
	}

	private void scheduleTomorrow(long chatId) throws TelegramApiException {
		String answerText = "расписание на завтра для id=" + chatId;
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
