package com.mycompany;

import com.google.gson.Gson;
import com.mycompany.my.MyTimerTask;
import com.mycompany.my.MyTimer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Класс-контроллер бота. Обрабатывает все входящие и исходящие данные, необходимые для работы бота:
 * хранит данные о юзерах;
 * получает котировку у API Мосбиржи;
 * планирует ежедневную автоматическую рассылку актуальной информации о котировке USD/RUB.
 */
public class TelegramBotController {

    // тикеры валютных пар
    public static final String USD_RUB_TICKER = "USD000UTSTOM";
    /*public static final String EUR_RUB_TICKER = "";
    public static final String CNY_RUB_TICKER = "";
    public static final String USD_KZT_TICKER = "";
    public static final String RUB_KZT_TICKER = "";
    public static final String BTC_RUB_TICKER = "";*/

    private TelegramBot bot; // ссылка на бота

    private String currencyQuote; // котировка валютной пары USD/RUB

    // Флаг актуальности котировки.
    // Нужен для того, чтобы при обращении к боту одновременно 100 юзеров, бот не отправлял одновременно 100 запросов к
    // бирже, а обратился к ней 1 раз, сохранил у себя в поле currencyQuote полученную котировку на время, равное длительности
    // актуальности котировки, и при обращениях юзеров в течение этого времени возвращал бы им эту сохраненную котировку.
    private volatile boolean isTickerQuoteRelevant;

    // длительность актуальности котировки
    private final int quoteRelevanceDuration = 5; // в минутах

    // Коллекция для хранения множества chatId пользователей бота
    private Set<Long> userChatIds = ConcurrentHashMap.newKeySet(); // так мы получаем потокобезопасный HashSet

    public void setBot(TelegramBot bot) {
        this.bot = bot;
    }

    public Set<Long> getChatIds() {
        return userChatIds;
    }

    // добавляет chatId нового юзера
    public void addUser(long userChatId) {
        userChatIds.add(userChatId);
    }

    // удаляет chatId юзера
    public void deleteUser(long userChatId) {
        userChatIds.remove(userChatId);
    }

    // удаляет все chatId всех юзеров
    public void deleteAllUsers() {
        userChatIds.clear();
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
    public void setTaskTimerOn(String startTimeStr, long period, TimeUnit unit) {
        String[] timeData = startTimeStr.split(" "); // timeData[0] - время, timeData[1] - часовой пояс
        var parsedTime = LocalTime.parse(timeData[0]);
        var zoneId = ZoneId.of(timeData[1]);

        // дата запуска - сегодня или завтра (см. коммент ниже)
        var startTime = ZonedDateTime.now(zoneId).with(parsedTime);
        // Если полученное время запуска сегодня уже прошло, то увеличиваем дату запуска на 1 день, таким образом
        // первый запуск состоится завтра в это же время.
        // Если еще не прошло, то оставляем без изменения, и запуск состоится сегодня в это время.
        if (startTime.isBefore(ZonedDateTime.now(zoneId))) {
            startTime = startTime.plusDays(1);
        }

        // Задача (task) для выполнения по таймеру MyTimer. Суть задачи: получаем котировку и рассылаем сообщение с ней всем юзерам
        MyTimerTask sendExchangeInfoDailyTask = () -> {
            currencyQuote = getQuoteFromMoex(USD_RUB_TICKER);
            bot.sendToAll(getExchangeInfoText(currencyQuote));
        };
        new MyTimer().schedule(sendExchangeInfoDailyTask, startTime, period, unit);
    }

    // возвращает текст сообщения с актуальной котировкой нашей валютной пары
    public String getRelevantExchangeInfo() {
        if (!isTickerQuoteRelevant) {
            currencyQuote = getQuoteFromMoex(USD_RUB_TICKER); // если котировка не актуальная, получаем актуальную у Мосбиржи
            isTickerQuoteRelevant = true; // и затем выставляем флаг актуальности

            // сбрасываем флаг через время, равное длительности актуальности котировки
            new MyTimer().schedule(() -> isTickerQuoteRelevant = false, quoteRelevanceDuration, TimeUnit.MINUTES);
        }
        return getExchangeInfoText(currencyQuote);
    }

    /** Получает котировку с Мосбиржи для данной валютной пары.
     * @param ticker - тикер данной валютной пары
     * */
    private String getQuoteFromMoex(String ticker) {
        final HttpURLConnection connection = sendHttpGetRequest(ticker);
        String response = getJsonResponse(connection);
        return getQuote(response);
    }

    // возвращает текст сообщения с котировками, который будет рассылаться юзерам
    private String getExchangeInfoText(String tickerQuote) {
        var timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        var currentMoscowZDT = ZonedDateTime.now(ZoneId.of("Europe/Moscow")); // текущие московское время и дата
        var currentMoscowTime = timeFormatter.format(currentMoscowZDT);

        return "Курсы валют на " + currentMoscowTime + " по мск:\n" +
                "1$ = " + tickerQuote + " RUB";
    }

    /** Отправляет http-get-запрос к API Мосбиржи (ISS MOEX API) на получение биржевой информации по данной
     * валютной паре.
     * @param ticker - тикер данной валютной пары
     * */
    private HttpURLConnection sendHttpGetRequest(String ticker) {
        final URL url;
        final HttpURLConnection con;
        try {
            url = new URL("https://iss.moex.com/iss/engines/currency/markets/selt/boards/CETS/securities/"
                    + ticker + ".json?iss.meta=off&iss.only=marketdata&marketdata.columns=SECID,LAST");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(10000); //
            con.setReadTimeout(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return con;
    }

    // читает json-ответ API Мосбиржи (ISS MOEX API) на наш http-запрос
    private String getJsonResponse(HttpURLConnection con) {
        final StringBuilder content = new StringBuilder();

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return content.toString();
    } // в пн 29 мая в 6:44-7:00 по мск курс был равен null

    // Получает котировку валютной пары из json-ответа.
    // Пример json-ответа на наш запрос приведен в файле /src/example/response.json.
    private String getQuote(String jsonString) {
        // парсим json в объект с помощью библиотеки GSON
        ExchangeData exchangeDataObj = new Gson().fromJson(jsonString, ExchangeData.class);

        // Для удобства работы данные из json-ответа "собираем" в map, в которой ключи - это элементы
        // массива "columns", а значения - элементы массива "data".
        Map<String, String> marketData = new HashMap<>();

        List<String> mapColumns = exchangeDataObj.getMarketdata().getColumns();
        List<String> mapData = exchangeDataObj.getMarketdata().getData();

        // заполняем
        for (int i = 0; i < mapData.size(); i++) {
            marketData.put(mapColumns.get(i), mapData.get(i));
        }

        // берем котировку из колонки "LAST", то есть last price данной валютной пары
        return marketData.get("LAST");
    }
}