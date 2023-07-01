package com.mycompany;

import com.mycompany.currency.CurrencyQuotes;
import com.mycompany.my.MyTimer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Класс, описывающий Telegram-бота.
 */
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

    // котировки валютных пар
    private CurrencyQuotes quotes = new CurrencyQuotes();

    // Коллекция для хранения множества chatId пользователей бота
    private Set<Long> userChatIds = ConcurrentHashMap.newKeySet(); // так мы получаем потокобезопасный HashSet

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
                    addUser(userChatId);

                    // отправляем сообщения с котировками фиатных валют и крипты пользователю
                    quotes.getRelevantQuotes();
                    send(userChatId, quotes.getFiatCurrenciesQuotesMessage());
                    send(userChatId, quotes.getCryptocurrenciesQuotesMessage());
                }
                case "/exit" -> {
                    send(userChatId, "Бот остановлен !");
                    deleteUser(userChatId);
                }
            }
        }
    }

    // добавляет chatId нового юзера
    public void addUser(long userChatId) {
        userChatIds.add(userChatId);
    }

    // удаляет chatId юзера
    public void deleteUser(long userChatId) {
        userChatIds.remove(userChatId);
    }

    /**
     * Создает и устанавливает таймер для периодической отправки биржевой информации всем юзерам бота.
     *
     * Timer и TimerTask - это служебные классы Java, используемые для планирования задач в фоновом потоке.
     * В двух словах: TimerTask is the task to perform and Timer is the scheduler.
     *
     * @param startTimeStr - время и часовой пояс первого запуска задачи (task) в виде текста в формате "11:00:00 Europe/Moscow"
     * @param period - период повтора задачи (task)
     * @param unit - единица измерения времени для period
     * */
    public void sendExchangeInfoToAllUsersAt(String startTimeStr, long period, TimeUnit unit) {
        String[] timeData = startTimeStr.split(" "); // timeData[0] - время, timeData[1] - часовой пояс
        var parsedTime = LocalTime.parse(timeData[0]);
        var zoneId = ZoneId.of(timeData[1]);

        // Дата запуска - сегодня или завтра (см. коммент ниже)
        var startTime = ZonedDateTime.now(zoneId).with(parsedTime);
        // Если полученное время запуска сегодня уже прошло, то увеличиваем дату запуска на 1 день, таким образом
        // первый запуск состоится завтра в это же время.
        // Если еще не прошло, то оставляем без изменения, и запуск состоится сегодня в это время.
        if (startTime.isBefore(ZonedDateTime.now(zoneId))) {
            startTime = startTime.plusDays(1);
        }

        new MyTimer().schedule(sendQuotesInfoMessageDailyTask, startTime, period, unit);
    }

    // Задача (task) для выполнения по таймеру MyTimer.
    // Суть задачи: получаем котировки фиатных валют и крипты и рассылаем сообщение с ними всем юзерам
    private MyTimer.MyTimerTask sendQuotesInfoMessageDailyTask = () -> {
        quotes.getRelevantQuotes();
        sendToAll(quotes.getFiatCurrenciesQuotesMessage());
        sendToAll(quotes.getCryptocurrenciesQuotesMessage());
    };

    /**
     * Отправляет сообщение всем зарегистрированным пользователям.
     * Если к моменту выполнения этого метода список пользователям будет пустой, цикл просто не запустится и все,
     * исключений никаких не будет, т.к. foreach при компиляции заменяется на while(iterator.hasNext()) { }
     *
     * @param text текст сообщения
     */
    private void sendToAll(String text) {
        SendMessage sendMessage = new SendMessage();
        try {
            for (long chatId : userChatIds) {
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