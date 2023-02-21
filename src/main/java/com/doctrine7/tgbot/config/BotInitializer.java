package com.doctrine7.tgbot.config;

import com.doctrine7.tgbot.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotInitializer {

    private final TelegramBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
            bot.updateCommands(getCommandsList());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private List<BotCommand> getCommandsList() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Регистрация участника"));
        listOfCommands.add(new BotCommand("/addreg", "Добавление сотрудника к рассылке"));
        listOfCommands.add(new BotCommand("/delreg", "Удаление сотрудника из рассылки"));
        listOfCommands.add(new BotCommand("/today", "Расписание на сегодня"));
        listOfCommands.add(new BotCommand("/tomorrow", "Расписание на завтра"));
        listOfCommands.add(new BotCommand("/thismonth", "расписание на текущий месяц"));
        listOfCommands.add(new BotCommand("/nextmonth", "расписание на следующий месяц"));
        listOfCommands.add(new BotCommand("/allemployees", "На кого получаю расписание"));
        listOfCommands.add(new BotCommand("/separated", "Получать расписание раздельно/вместе"));
        return listOfCommands;
    }

}
