package com.doctrine7.tgbot.service;

import com.doctrine7.tgbot.config.BotConfig;
import com.doctrine7.tgbot.model.*;
import com.doctrine7.tgbot.model.exceptions.CustomBannedUserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    private final SQLDatabaseConnection connection;
    private final PasswordGenerator passwordGenerator;
    private static final String REGISTRATION_TEXT = "Введите одним сообщением ОТВЕТОМ НА ЭТО СООБЩЕНИЕ! текущий " +
            "пароль (узнать " +
            "можно у системного администратора) " +
            "и полное ФИО (или название кабинета) как в 1С, например:\n\n" +
            "password Иванов Иван Иванович";
    private static final int REGISTRATION_ATTEMPTS = 10;
    private final DateTimeFormatter dateFormatForRecognizeDateFromButton = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getChatId() > 0) {
            String text = update.getMessage().getText(); //команда
            long chatId = update.getMessage().getChatId();
            Optional<User> optionalUser = userRepository.findById(chatId);

            if (optionalUser.isPresent()) { //если пользователь зарегистрирован
                if (checkBannedUsers(optionalUser.get(), chatId)) { //и превышены попытки добавления расписания
                    throw new CustomBannedUserException("User with id=" + chatId + " is banned and try to use bot");
                }
            }

            if (checkScheduleUserRegistration(update, chatId, text)) { //попытка добавления расписания?
                return;
            }

            if (deleteScheduleUserRegistration(update, chatId, text)) { //попытка изменения расписания?
                return;
            }

            switch (text) { //не забанен и это не регистрация
                case "/start":
                    try {
                        regCommand(chatId);
                        registerUser(update.getMessage());
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                    break;
                case "/today":
                    if (optionalUser.isPresent()) {
                        try {
                            scheduleByDate(chatId, optionalUser.get(), LocalDate.now());
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                        }
                    }
                    break;
                case "/tomorrow":
                    if (optionalUser.isPresent()) {
                        try {
                            scheduleByDate(chatId, optionalUser.get(), LocalDate.now().plusDays(1));
                        } catch (TelegramApiException e) {
                            log.error(e.getMessage());
                        }
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
                        log.error("to user with chat id {} sent registration text", chatId);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                    break;
                case "/allemployees":
                    text = optionalUser.map(user -> "Вы получаете расписание для: \n"
                            + user.allEmployeesToMessage(employeeRepository)).orElse("Вы не зарегистрированы, сначала введите " +
                            "команду /start");
                    log.warn("request for all employees for shedule sending");
                    try {
                        sendMessageToId(chatId, text);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                    break;
                case "/delreg":
                    text = optionalUser.map(user -> "Вы получаете расписание для: \n"
                                    + user.allEmployeesToMessage(employeeRepository) + " введите ОТВЕТОМ НА ЭТО СООБЩЕНИЕ номер " +
                                    "сотрудника " +
                                    "которого удаляем.")
                            .orElse("Вы не зарегистрированы, сначала введите команду /start");
                    try {
                        sendMessageToId(chatId, text);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                    break;
                case "/thismonth":
                    SendMessage outMess = new SendMessage();
                    LocalDate init = LocalDate.of(LocalDate.now().getYear(),
                            LocalDate.now().getMonth(), 1);
                    outMess.setReplyMarkup(initKeyboard(init));
                    outMess.setChatId(chatId);
                    outMess.setText("Выберите дату");
                    try {
                        execute(outMess);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    log.warn("this month with keyboard requested");
                    break;
                case "/nextmonth":
                    outMess = new SendMessage();
                    init = LocalDate.of(LocalDate.now().getYear(),
                            LocalDate.now().plusMonths(1).getMonth(), 1);
                    outMess.setReplyMarkup(initKeyboard(init));
                    outMess.setChatId(chatId);
                    outMess.setText("Выберите дату");
                    try {
                        execute(outMess);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    log.warn("next month with keyboard requested");
                    break;
                case "/separated":
                    if (isRegistered(chatId)) {
                        try {
                            setSeparatedShedule(chatId);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                default:
                    if (optionalUser.isPresent() && text.length() == 5) {
                        try {
                            LocalDate ldForShedule =
                                    LocalDate.parse(text.concat(".").concat("" + LocalDate.now().getYear())
                                            , dateFormatForRecognizeDateFromButton);

                            if (ldForShedule.getMonth().getValue() == 1 && LocalDate.now().getMonth().getValue() == 12) {
                                ldForShedule = ldForShedule.plusYears(1);
                            }
                            scheduleByDate(chatId, optionalUser.get(), ldForShedule);
                            break;
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        } catch (DateTimeParseException e) {
                            System.out.println(e.getMessage());
                        }
                    }


                    try {
                        sendMessageToId(chatId, "Команда не найдена!");
                        log.error("unrecognized command " + text + " from user @" + update.getMessage().getChat().getUserName());
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
            }

        }

    }

    public void updateCommands(List<BotCommand> listOfCommands) {
        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), "ru"));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void setSeparatedShedule(long chatId) throws TelegramApiException {
        var userOpt = userRepository.findById(chatId);
        String text;
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getSeparatedShedule() == null) {
                user.setSeparatedShedule(true);
                text = "Вы будете получать раздельное расписание";
            } else {
                user.setSeparatedShedule(!user.getSeparatedShedule());
                if (!user.getSeparatedShedule()) {
                    text = "Вы будете получать расписание на все кабинеты сразу";
                } else {
                    text = "Вы будете получать раздельное расписание";
                }
            }
            userRepository.save(user);
            sendMessageToId(chatId, text);
        }
    }


    private boolean checkBannedUsers(User user, Long chatId) {
        if (user.getRegistrationAttempts() >= REGISTRATION_ATTEMPTS) {
            try {
                log.error("Попытка регистрации заблокированного пользователя");
                sendMessageToId(chatId, "Увы, пользователь отключен за превышение попыток регистрации");
                return true;
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        return false;
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
            log.warn("user {} registered ",
                    (message.getChat().getUserName() == null) ? message.getChatId() : message.getChat().getUserName());
        } else {
            log.warn("Attempt to re-register the user {}",
                    (message.getChat().getUserName() == null) ? message.getChatId() : message.getChat().getUserName());
        }
    }

    private void updateUser(long chatId, Message message) throws TelegramApiException {
        Optional<User> userOpt = userRepository.findById(chatId);
        String employeeToRequest = message.getText().substring(message.getText().indexOf(" ") + 1);
        String employee = connection.sendRegistrationRequest(employeeToRequest);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRegistrationAttempts(0); //обнулим попытки регистрации, пароль введен верно
            if (employee.equals("")) {
                userRepository.save(user);
                String textToSend;
                if (employeeToRequest.equals(message.getText())) {
                    textToSend = "Вы забыли добавить сотрудника через пробел после пароля";
                } else {
                    textToSend = String.format("Сотрудник %s не найден в 1С!", employee);
                }
                sendMessageToId(chatId, textToSend);
                log.warn("new incorrect attempt to add employee {} for chat id {} ", employee, chatId);
                return;
            }
            if (user.getEmployees(employeeRepository).contains(employee)) {
                userRepository.save(user);
                sendMessageToId(chatId, String.format("Сотрудник %s уже добавлен!", employee));
                return;
            }
            user.addEmployee(employee, employeeRepository);
            sendMessageToId(chatId, String.format("Сотрудник %s успешно связан с вашим id ", employee));
            var listOfRelatedEmployees = connection.checkRelatedEmployees(employee);
            if (listOfRelatedEmployees.size() > 0) {
                listOfRelatedEmployees.forEach(u -> user.addEmployee(u, employeeRepository));
            }
            if (user.getEmployees(employeeRepository).size() > 1) {
                sendMessageToId(chatId, "так же получаете расписание для: \n" + user.allEmployeesToMessage(employeeRepository));
            }
            userRepository.save(user);
            log.warn("new correct attempt to add employee {} for chat id {} ", employee, chatId);
        }
    }

    private boolean isRegistered(long id) {
        return userRepository.findById(id).isPresent();
    }

    private void regCommand(long chatId) throws TelegramApiException {
        String answerText = "reg from id=" + chatId;
        sendMessageToId(chatId, answerText);
    }

    private void scheduleByDate(long chatId, User user, LocalDate date) throws TelegramApiException {
        String answerText = "расписание на " + date + " для id=" + chatId + "\n";
        log.warn("Requesting shedule for date {} by user {}, chat id = {}", date, user.getUserName(), chatId);
        List<Shedule> answerList = connection.sendScheduleRequest(date);
        SheduleService sheduleService = new SheduleService(answerList, employeeRepository);
        StringBuilder sb = new StringBuilder();
        var listShedule = sheduleService.actualizeByEmployee(user);
        listShedule.forEach(sh -> sb.append(sh.toString()));
        String answer = sb.toString();
        if (answer.length() > 4000) {
            answerText = answerText + "\n" + answer.substring(0, 4000);
            sendMessageToId(chatId, answerText);
            answerText = answer.substring(4000);
        } else {
            answerText = answerText + answer;
        }

        sendMessageToId(chatId, answerText);
    }


    public void sendMessageToId(long chatId, String textToSend) throws TelegramApiException {
        SendMessage outputMessage = new SendMessage();
        outputMessage.setChatId(chatId);
        if (chatId < 0) {
            outputMessage.enableMarkdownV2(true);
        }
        outputMessage.setText(textToSend);
        outputMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        try {
            execute(outputMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }


    private boolean checkScheduleUserRegistration(Update update, Long chatId, String text) {
        if (update.getMessage().isReply()
                && REGISTRATION_TEXT.equals(update.getMessage().getReplyToMessage().getText())
                && isRegistered(chatId)) {
            String password = passwordGenerator.getActualPassword();
            if (text.contains(password)) {
                try {
                    updateUser(chatId, update.getMessage());
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
                return true;
            } else {
                User user = userRepository.findById(chatId).get();
                user.setRegistrationAttempts(user.getRegistrationAttempts() + 1);
                userRepository.save(user);
                log.error("INCORRECT PASSWORD from user with chat id {}", chatId);
                try {
                    String textToSend =
                            REGISTRATION_ATTEMPTS - user.getRegistrationAttempts() > 0 ?
                                    "У вас осталось " + (REGISTRATION_ATTEMPTS - user.getRegistrationAttempts()) + " попыток" :
                                    "У вас не осталось попыток, вы заблокированы";
                    sendMessageToId(chatId, textToSend);
                    return true;
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return false;
    }

    private boolean deleteScheduleUserRegistration(Update update, Long chatId, String text) {
        if (update.getMessage().isReply()
                && update.getMessage().getReplyToMessage().getText().contains("введите ОТВЕТОМ НА ЭТО СООБЩЕНИЕ номер" +
                " сотрудника которого удаляем.")
                && isRegistered(chatId)) {

            try {
                int nom = Integer.parseInt(text);
                User user = userRepository.findById(chatId).get();
                user.deleteEmployee(nom, employeeRepository);
                userRepository.save(user);
                sendMessageToId(chatId, "Сотрудник успешно удален из выдачи расписания");
                return true;
            } catch (TelegramApiException | NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private ReplyKeyboardMarkup initKeyboard(LocalDate localDate) {
        //Создаем объект будущей клавиатуры и выставляем нужные настройки
        var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true); //подгоняем размер
        replyKeyboardMarkup.setOneTimeKeyboard(true); //скрываем после использования

        //Создаем список с рядами кнопок
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        //Создаем один ряд кнопок и добавляем его в список
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRows.add(keyboardRow);
        //Добавляем кнопки с текстом в наш ряд
        LocalDate endDay = localDate.withDayOfMonth(localDate.lengthOfMonth());
        int i = 0;
        //подгоняем расположение кнопок под календарные дни недели начала месяца
        while (localDate.getDayOfWeek().getValue() > keyboardRow.size() + 1) {
            keyboardRow.add(" ");
            i++;
        }

        while (localDate.isBefore(endDay) || localDate.isEqual(endDay)) {
            keyboardRow.add(new KeyboardButton(localDate.format(DateTimeFormatter.ofPattern("dd.MM"))));
            localDate = localDate.plusDays(1);
            i++;
            if (i >= 7) {
                keyboardRow = new KeyboardRow();
                keyboardRows.add(keyboardRow);
                i = 0;
            }
        }
        while (keyboardRow.size() < 7) { //а тут подгонка в конце
            keyboardRow.add(" ");
            i++;
        }
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
