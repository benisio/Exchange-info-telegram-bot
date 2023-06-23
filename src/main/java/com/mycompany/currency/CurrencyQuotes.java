package com.mycompany.currency;

import com.mycompany.Utilities;
import com.mycompany.my.MyTimer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mycompany.currency.MoexCurrencyPair.*;
import static com.mycompany.currency.CalculatedQuoteCurrencyPair.*;

/**
 * Класс, хранящий в себе Map с котировками.
 * Оформил в виде отдельного класса, делегирующий свое поведение объекту Map, так как стандартного функционала Map
 * недостаточно, нужны дополнительные методы.
 */
public class CurrencyQuotes {

    private Map<CurrencyPair, Double> currencyQuotes = new LinkedHashMap<>();

    // Флаг актуальности котировки.
    // Нужен для того, чтобы при обращении к боту одновременно 100 юзеров, бот не отправлял одновременно 100 запросов к
    // бирже, а обратился к ней 1 раз, сохранил у себя в поле currencyQuotes полученные котировки на время, равное
    // длительности актуальности котировки, и при обращениях юзеров в течение этого времени возвращал бы им эти
    // сохраненные котировки.
    private volatile boolean quotesRelevant;

    // длительность актуальности котировок
    private final int quotesRelevanceDuration = 5; // в минутах

    // получает актуальные котировки для каждой валютной пары
    private void getRelevantQuotes() {
        // получаем и кладем в map
        currencyQuotes.put(USD_RUB,   USD_RUB.getQuote());
        currencyQuotes.put(EUR_RUB,   EUR_RUB.getQuote());
        currencyQuotes.put(CNY_RUB,   CNY_RUB.getQuote());
        currencyQuotes.put(TRY_RUB,   TRY_RUB.getQuote());
        currencyQuotes.put(EUR_USD,   EUR_USD.getQuote());
        currencyQuotes.put(USD_KZT,   USD_KZT.getQuote());
        currencyQuotes.put(RUB_KZT,   RUB_KZT.getQuote());

        quotesRelevant = true; // устанавливаем флаг актуальности котировок

        // через 5 мин (длительность актуальности котировок, quotesRelevanceDuration) сбрасываем флаг актуальности
        new MyTimer().schedule(() -> quotesRelevant = false, quotesRelevanceDuration, TimeUnit.MINUTES);
    }

    // Возвращает текст сообщения с котировками, которое будет отправлено пользователям
    public String getQuotesInfoMessage() {
        // обновляем котировки, если они неактуальны
        if (!quotesRelevant) {
            getRelevantQuotes();
        }

        // получаем время последнего обновления котировок
        String quotesUpdateFormattedTime = getQuotesUpdateFormattedTime();

        // формируем текст сообщения для отправки пользователям
        StringBuilder messageBuilder = new StringBuilder("Курсы валют на " + quotesUpdateFormattedTime + " по мск:\n");
        currencyQuotes.forEach((currencyPair, quote) -> {
            String firstCurrency = currencyPair.getFirstCurrencyCode();
            String quoteStr = Utilities.formatDouble(quote);
            String secondCurrency = currencyPair.getSecondCurrencyCode();

            // метод String.format() заполняет шаблон строки (первый аргумент) строковыми вставками (последующие аргументы)
            String currencyPairMessage = String.format("\n1 %s = %s %s", firstCurrency, quoteStr, secondCurrency);
            messageBuilder.append(currencyPairMessage);
        });

        return messageBuilder.toString();
    }

    // получает на Мосбирже время последнего обновления котировок (UPDATETIME) и возвращает его в формате "11:00"
    private String getQuotesUpdateFormattedTime() {
        // получаем на Мосбирже время последнего обновления в формате "11:00:00"
        var parseFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String quotesUpdateTimeStr = USD_RUB.getQuotesUpdateTime();
        var quotesUpdateTime = LocalTime.parse(quotesUpdateTimeStr, parseFormatter);

        // форматируем полученное время - убираем секунды
        var timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return timeFormatter.format(quotesUpdateTime);
    }
}