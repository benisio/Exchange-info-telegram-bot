package com.mycompany;

import com.mycompany.config.HibernateSessionFactoryUtil;
import com.mycompany.config.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.concurrent.TimeUnit;

/**
 * Entrance point of the application.
 * */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        HibernateSessionFactoryUtil.init();

        // создаем и регистрируем бота
        TelegramBot bot = new TelegramBot(new TelegramBotConfig());
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        // планируем автоматическую отправку ботом сообщения с котировками ежедневно в 11:00 по мск
        bot.sendExchangeInfoToAllUsersAt("11:00:00 Europe/Moscow", 1, TimeUnit.DAYS);
    }
}