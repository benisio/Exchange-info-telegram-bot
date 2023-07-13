package com.mycompany;

import com.mycompany.config.TestBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Entrance point of the application.
 */
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        // создаем и регистрируем бота
        TelegramBot bot = new TelegramBot(new TestBotConfig()); // передавать сюда параметром BotConfig, чтобы можно было разделить в коде тестового бота и боевого
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        // должен ли этот кусок кода быть здесь ?
        // если у нас нет юзеров, кому мы будем отправлять сообщения ?
        // планируем автоматическую отправку ботом сообщения с котировками ежедневно в 11:00 по мск
        // bot.sendExchangeInfoToAllUsersAt("11:00:00 Europe/Moscow", 1, TimeUnit.DAYS);

        // для тестирования работы отображения крипты
        var pattern = DateTimeFormatter.ofPattern("HH:mm:ss VV");
        var almostNow = ZonedDateTime.now().plusMinutes(1).format(pattern);
        System.out.println(almostNow);
        bot.sendExchangeInfoToAllUsersAt(almostNow, 1, TimeUnit.HOURS);
    }
}