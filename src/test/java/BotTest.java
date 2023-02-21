import com.doctrine7.tgbot.config.BotConfig;
import com.doctrine7.tgbot.model.*;
import com.doctrine7.tgbot.model.exceptions.CustomBannedUserException;
import com.doctrine7.tgbot.service.TelegramBot;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class BotTest {
    @Value("${test.bot.name}")
    private String botName;
    @Value("${test.bot.token}")
    private String token;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    SQLDatabaseConnection sqlDatabaseConnection;
    TelegramBot telegramBot;
    User user1; //banned
    User user2; //0 employees
    User user3; //correct

    Employee employee;

    @BeforeEach
    void setup() {
        user1 = new User();
        user1.setUserName("banned");
        user1.setChatId(1L);
        user1.setRegistrationAttempts(10);

        user2 = new User();
        user2.setChatId(2L);
        user2.setUserName("no employees");
        user2.setRegistrationAttempts(9);

        user3 = new User();
        user3.setChatId(3L);
        user3.setRegistrationAttempts(0);
        employee = new Employee();
        employee.setId(33L);
        employee.setEmployee("test employee");
        employee.setUser_id(3L);

        BotConfig botConfig = new BotConfig();
        botConfig.setBotName(botName);
        botConfig.setToken(token);
        telegramBot = new TelegramBot(botConfig, userRepository, employeeRepository, sqlDatabaseConnection);

    }

    @Test
    void FailToBannedUserTryToSendMessageExceptException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Chat chat = new Chat();
        chat.setId(1L);
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(chat);
        message.setText("text");
        Update update = new Update();
        update.setMessage(message);
        Assertions.assertThrows(CustomBannedUserException.class, () ->
                telegramBot.onUpdateReceived(update));
    }

    @Test
    void addRegistrationAttemptToUserWithNineAttemptsAndExceptException() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user2));
        Chat chat = new Chat();
        chat.setId(2L);
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(chat);
        message.setText("text");
        Message messageRegistration = new Message();
        messageRegistration.setText("Введите одним сообщением ОТВЕТОМ НА ЭТО СООБЩЕНИЕ! текущий " +
                "пароль (узнать " +
                "можно у системного администратора) " +
                "и полное ФИО (или название кабинета) как в 1С, например:\n\n" +
                "password Иванов Иван Иванович");
        message.setReplyToMessage(messageRegistration);
        Update update = new Update();
        update.setMessage(message);
        Assertions.assertDoesNotThrow(() ->
                telegramBot.onUpdateReceived(update)); //last attempt


        Chat chat2 = new Chat();
        chat2.setId(2L);
        Message message2 = new Message();
        message2.setMessageId(1);

        message2.setReplyToMessage(messageRegistration);
        message2.setChat(chat);
        message2.setText("incorrect pass");

        Update update2 = new Update();
        update2.setMessage(message2);

        Assertions.assertThrows(CustomBannedUserException.class, () ->
                telegramBot.onUpdateReceived(update2)); //banned
    }

    @Test
    void lastCorrectRegistrationAttemptWithNineAttemptsAndExceptNoException() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user2));
        Mockito.when(sqlDatabaseConnection.sendRegistrationRequest(Mockito.anyString()))
                .thenReturn("");
        Chat chat = new Chat();
        chat.setId(2L);
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(chat);
        message.setText(new PasswordGenerator().getActualPassword());
        Message messageRegistration = new Message();
        messageRegistration.setText("Введите одним сообщением ОТВЕТОМ НА ЭТО СООБЩЕНИЕ! текущий " +
                "пароль (узнать " +
                "можно у системного администратора) " +
                "и полное ФИО (или название кабинета) как в 1С, например:\n\n" +
                "password Иванов Иван Иванович");
        message.setReplyToMessage(messageRegistration);
        Update update = new Update();
        update.setMessage(message);
        Assertions.assertDoesNotThrow(() ->
                telegramBot.onUpdateReceived(update)); //last attempt


        Chat chat2 = new Chat();
        chat2.setId(2L);
        Message message2 = new Message();
        message2.setMessageId(1);

        message2.setReplyToMessage(messageRegistration);
        message2.setChat(chat);
        message2.setText("incorrect pass");

        Update update2 = new Update();
        update2.setMessage(message2);

        Assertions.assertDoesNotThrow(() ->
                telegramBot.onUpdateReceived(update)); //not banned
    }
}
