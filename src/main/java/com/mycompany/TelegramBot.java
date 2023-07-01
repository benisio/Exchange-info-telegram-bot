package com.mycompany;

import com.mycompany.currency.*;
import com.mycompany.model.User;
import com.mycompany.currency.CurrencyQuotes;
import com.mycompany.config.TelegramBotConfig;
import com.mycompany.my.MyTimer;
import com.mycompany.service.UserService;
import com.mycompany.service.UserServiceImpl;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.mycompany.currency.BybitCryptocurrencyPair.*;
import static com.mycompany.currency.CalculatedQuoteCurrencyPair.*;
import static com.mycompany.currency.MoexCurrencyPair.*;

/**
 * Класс, описывающий Telegram-бота.
 */
/*@Component*/
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;
    private UserService userService = new UserServiceImpl();

    // котировки валютных пар
    private CurrencyQuotes quotes = new CurrencyQuotes();

    // Коллекция для хранения множества chatId пользователей бота
    private Set<Long> userChatIds = ConcurrentHashMap.newKeySet(); // так мы получаем потокобезопасный HashSet

    public TelegramBot(TelegramBotConfig botConfig) {
        this.botConfig = botConfig;
    }

    // возвращает username бота
    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername(); // этот параметр можно получить у телеграм-бота @BotFather https://t.me/BotFather
    }

    // возвращает токен бота
    @Override
    public String getBotToken() {
        return botConfig.getBotToken(); // этот параметр можно получить у телеграм-бота @BotFather https://t.me/BotFather
    }

    private User user = readUserFromDataBase();

    // имитируем чтение инфы из базы
    private User readUserFromDataBase() {
        Map<String, Boolean> settings = new HashMap<>();
        settings.put(USD_RUB.name(), false);
        settings.put(EUR_RUB.name(), false);
        settings.put(CNY_RUB.name(), false);
        settings.put(TRY_RUB.name(), false);
        settings.put(EUR_USD.name(), false);
        settings.put(USD_KZT.name(), false);
        settings.put(RUB_KZT.name(), false);
        settings.put(USD_BYN.name(), false);
        settings.put(BTC_USDT.name(), true);
        settings.put(ETH_USDT.name(), true);
        settings.put(SOL_USDT.name(), false);
        settings.put(WLKN_USDT.name(), true);

        return new User(3, "den", "chik", "pyramid", settings);
    }

    // вызывается автоматически всякий раз при получении сообщения (update) от юзера
    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, содержит ли update сообщение и содержится ли в сообщении текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText(); // получаем текст сообщения
            long chatId = update.getMessage().getChatId();

            // обработка нажатий пунктов меню
            switch (messageText) {
                case "/start" -> {
                    if (!userExists(chatId)) {
                        addUser(chatId); // ???????? нужна ли эта строка ?

                        String firstName = update.getMessage().getFrom().getFirstName();
                        String lastName = update.getMessage().getFrom().getLastName();
                        String userName = update.getMessage().getFrom().getUserName();
                        userService.add(new User(chatId, firstName, lastName, userName));
                    }

                    // отправляем сообщения с котировками фиатных валют и крипты пользователю
                    quotes.getRelevantQuotes();
                    sendMessage(chatId, quotes.getFiatCurrenciesQuotesMessage());
                    sendMessage(chatId, quotes.getCryptocurrenciesQuotesMessage());
                }

                case "/settings" -> {
                    sendMessage(chatId, "Представим, что появилась клавиатура !");
                }

                case "/exit" -> {
                    sendMessage(chatId, "Бот остановлен !");
                    deleteUser(chatId);
                    userService.delete(new User(chatId));
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

    private boolean userExists(long chatId) {
        return userService.getByChatId(chatId) != null;
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
     */
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
                sendMessage.setChatId(chatId); // почему я здесь не юзаю метод send() ????
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
     * @param chatId id чата юзера, которому будет отправлено сообщение
     * @param text текст сообщения
     */
    private void sendMessage(long chatId, String text) {
        try {
            execute(new SendMessage("" + chatId, text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}