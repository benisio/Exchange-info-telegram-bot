package com.mycompany;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс конфигурации Telegram-бота
 * */
@Component
public class TelegramBot extends TelegramLongPollingBot {

    // читаем properties-файл с конфигурационными параметрами бота
    static Properties botProperties;
    static {
        // try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
        try (InputStream resourceStream = TelegramBot.class.getClassLoader().getResourceAsStream("application.properties")) {
            botProperties = new Properties();
            botProperties.load(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final TelegramBotController botController = new TelegramBotController();

    public TelegramBotController getController() {
        botController.setBot(this);
        return botController;
    }

    // возвращает username бота
    @Override
    public String getBotUsername() {
        return botProperties.getProperty("bot.username"); // этот параметр можно получить у телеграм-бота @BotFather https://t.me/BotFather
    }

    // возвращает токен бота
    @Override
    public String getBotToken() {
        return botProperties.getProperty("bot.token"); // этот параметр можно получить у телеграм-бота @BotFather https://t.me/BotFather
    }

    // вызывается автоматически всякий раз при получении сообщения (update) от юзера
    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, содержит ли update сообщение и содержится ли в сообщении текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText(); // получаем текст сообщения
            long userChatId = update.getMessage().getChatId();

            // обработка нажатий пунктов меню
            switch (text) {
                case "/start" -> {
                    botController.addUser(userChatId);
                    String relevantExchangeInfo = botController.getRelevantExchangeInfo();
                    send(userChatId, relevantExchangeInfo);
                }
                case "/exit" -> {
                    send(userChatId, "Бот остановлен !");
                    botController.deleteUser(userChatId);
                }
            }
        }
    }

    // отправляет сообщение всем зарегистрированным юзерам
    public void sendToAll(String text) {
        SendMessage sendMessage = new SendMessage();
        try {
            for (long chatId : botController.getChatIds()) {
                sendMessage.setChatId(chatId);
                sendMessage.setText(text);
                execute(sendMessage); // отправляем сообщение
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение одному пользователю.
     *
     * @param userChatId id чата юзера, которому будет отправлено сообщение
     * @param text текст сообщения
     */
    private void send(long userChatId, String text) {
        try {
            execute(new SendMessage("" + userChatId, text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}