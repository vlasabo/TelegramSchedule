package com.doctrine7.TGbot.service;

import com.doctrine7.TGbot.config.BotConfig;
import com.doctrine7.TGbot.config.ResponseToSqlConfig;
import com.doctrine7.TGbot.model.PasswordGenerator;
import com.doctrine7.TGbot.model.SQLDatabaseConnection;
import com.doctrine7.TGbot.model.User;
import com.doctrine7.TGbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.Optional;


@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

	final BotConfig config;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ResponseToSqlConfig configSql;
	@Autowired
	private SQLDatabaseConnection connection;
	private String password;
	private static final String REGISTRATION_TEXT = "Введите одним сообщением В ОТВЕТ НА ЭТО текущий пароль (узнать " +
			"можно у системного администратора) " +
			"и полное ФИО (или название кабинета) как в 1С, например:\n\n" +
			"password Иванов Иван Иванович";
	private static final int REGISTRATION_ATTEMPTS = 10;

	public TelegramBot(BotConfig config) {
		this.config = config;
		List<BotCommand> listOfCommands = new ArrayList<>();
		listOfCommands.add(new BotCommand("/start", "Регистрация участника"));
		listOfCommands.add(new BotCommand("/addreg", "Добавление сотрудника к рассылке"));
		listOfCommands.add(new BotCommand("/today", "Расписание на сегодня"));
		listOfCommands.add(new BotCommand("/tomorrow", "Расписание на завтра"));
		try {
			execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
		} catch (TelegramApiException e) {
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
			password = new PasswordGenerator().getActualPassword();
			Optional<User> optionalUser = userRepository.findById(chatId);

			//TODO: вынести это всё в отдельный логический блок
			if (optionalUser.isPresent()) { //если пользователь зарегистрирован

				//и превышены попытки добавления расписания
				if (optionalUser.get().getRegistrationAttempts() >= REGISTRATION_ATTEMPTS) {
					try {
						sendMessageToId(chatId, "Увы, пользователь отключен за превышение попыток регистрации");
						return;
					} catch (TelegramApiException e) {
						log.error("Попытка регистрации заблокированного пользователя");
					}
				}
			}

			if (update.getMessage().isReply()
					&& REGISTRATION_TEXT.equals(update.getMessage().getReplyToMessage().getText())
					&& isRegistered(chatId)) {

				if (text.contains(password)) {
					try {
						updateUser(chatId, update.getMessage());
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
					return;
				} else {
					User user = userRepository.findById(chatId).get();
					user.setRegistrationAttempts(user.getRegistrationAttempts() + 1);
					userRepository.save(user);
					try {
						sendMessageToId(chatId, "У вас осталось " + (REGISTRATION_ATTEMPTS - user.getRegistrationAttempts()) + " попыток");
						return;
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
				}
			}

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
				case "/addreg":
					if (isRegistered(chatId)) {
						text = REGISTRATION_TEXT;
					} else {
						text = "Вы не зарегистрированы, сначала введите команду /start";
					}

					try {
						sendMessageToId(chatId, text);
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
					break;
				default:
					try {
						sendMessageToId(chatId, "Command not found!");
						log.debug("unrecognized command " + text + " from user @" + update.getMessage().getChat().getUserName());
					} catch (TelegramApiException e) {
						log.error(e.getMessage());
					}
			}

		}
	}


	private void registerUser(Message message) {
		if (userRepository.findById(message.getChatId()).isEmpty()) {
			Long chatId = message.getChatId();
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
			log.error("Attempt to re-register the user " + message.getChat().getUserName());
		}
	}

	private void updateUser(long chatId, Message message) throws TelegramApiException {
		Optional<User> user = userRepository.findById(chatId);
		String employee = message.getText().substring(message.getText().indexOf(" "));
		if (user.isPresent()) {
			user.get().addEmployee(employee);
			userRepository.save(user.get());
			sendMessageToId(chatId, String.format("Сотрудник %s успешно связан с вашим id ", employee));
		}
	}

	private boolean isRegistered(long id) {
		return userRepository.findById(id).isPresent();
	}

	private void regCommand(long chatId) throws TelegramApiException {
		String answerText = "reg from id=" + chatId;
		sendMessageToId(chatId, answerText);
	}

	private void scheduleToday(long chatId) throws TelegramApiException {
		String answerText = "расписание на сегодня для id=" + chatId + "\n";
		connection.sendRequest(LocalDate.now());
		if (connection.getResponse().length() > 4000) {
			answerText = answerText + "\n" + connection.getResponse().substring(0, 4000);
		} else {
			answerText = answerText + connection.getResponse();
		}
		sendMessageToId(chatId, answerText);
	}

	private void scheduleTomorrow(long chatId) throws TelegramApiException {
		String answerText = "расписание на завтра для id=" + chatId + "\n";
		connection.sendRequest(LocalDate.now().plusDays(1));
		if (connection.getResponse().length() > 4000) {
			answerText = answerText + "\n" + connection.getResponse().substring(0, 4000);
		} else {
			answerText = answerText + connection.getResponse();
		}
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
