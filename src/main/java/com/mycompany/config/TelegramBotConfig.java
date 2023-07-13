package com.mycompany.config;

import com.mycompany.TelegramBot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TelegramBotConfig {

    // читаем properties-файл с конфигурационными параметрами бота
    static Properties botProperties;
    static { // вроде как в спринге ресурсы подтягиваются автоматически из этого файла
        // try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
        try (InputStream resourceStream = TelegramBot.class.getClassLoader().getResourceAsStream("application.properties")) {
            botProperties = new Properties();
            botProperties.load(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String botUsername = botProperties.getProperty("bot.username");

    private String botToken = botProperties.getProperty("bot.token");

    // возвращает username бота
    public String getBotUsername() {
        return botUsername;
    }

    // возвращает токен бота
    public String getBotToken() {
        return botToken;
    }
}