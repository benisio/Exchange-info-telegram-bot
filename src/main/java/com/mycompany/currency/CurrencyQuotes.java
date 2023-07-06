package com.mycompany.currency;

import com.mycompany.Utilities;
import com.mycompany.my.MyTimer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mycompany.currency.MoexCurrencyPair.*;
import static com.mycompany.currency.BybitCryptocurrencyPair.*;
import static com.mycompany.currency.CalculatedQuoteCurrencyPair.*;

/**
 * Класс, хранящий в себе Map с котировками.
 * Оформил в виде отдельного класса, делегирующий свое поведение объекту Map, так как стандартного функционала Map
 * недостаточно, нужны дополнительные методы.
 */
public class CurrencyQuotes {

    // заводим отдельные мапы для крипты и фиатных валют, так как будем отправлять их котировки в разных сообщениях
    private Map<CurrencyPair, Double> fiatCurrencyQuotes = new LinkedHashMap<>();
    private Map<CurrencyPair, Double> cryptoCurrencyQuotes = new LinkedHashMap<>();

    // Флаг актуальности котировки.
    // Нужен для того, чтобы при обращении к боту одновременно 100 юзеров, бот не отправлял одновременно 100 запросов к
    // бирже, а обратился к ней 1 раз, сохранил у себя в поле currencyQuotes полученные котировки на время, равное
    // длительности актуальности котировки, и при обращениях юзеров в течение этого времени возвращал бы им эти
    // сохраненные котировки.
    private volatile boolean quotesRelevant;

    // получает актуальные котировки для каждой валютной пары
    public void getRelevantQuotes() {
        if (!quotesRelevant) { // если котировки неактуальны, получаем актуальные и кладем в mapы

            // фиатные валюты
            fiatCurrencyQuotes.put(USD_RUB,   USD_RUB.getQuote());
            fiatCurrencyQuotes.put(EUR_RUB,   EUR_RUB.getQuote());
            fiatCurrencyQuotes.put(CNY_RUB,   CNY_RUB.getQuote());
            fiatCurrencyQuotes.put(TRY_RUB,   TRY_RUB.getQuote());
            fiatCurrencyQuotes.put(EUR_USD,   EUR_USD.getQuote());
            fiatCurrencyQuotes.put(USD_KZT,   USD_KZT.getQuote());
            fiatCurrencyQuotes.put(RUB_KZT,   RUB_KZT.getQuote());

            // крипта
            cryptoCurrencyQuotes.put(BTC_USDT,  BTC_USDT.getQuote());
            cryptoCurrencyQuotes.put(ETH_USDT,  ETH_USDT.getQuote());
            cryptoCurrencyQuotes.put(SOL_USDT,  SOL_USDT.getQuote());
            cryptoCurrencyQuotes.put(WLKN_USDT, WLKN_USDT.getQuote());

            quotesRelevant = true; // устанавливаем флаг актуальности котировок

            // длительность актуальности котировок
            final int quotesRelevanceDuration = 5; // в минутах
            // через 5 мин (длительность актуальности котировок, quotesRelevanceDuration) сбрасываем флаг актуальности
            new MyTimer().schedule(() -> quotesRelevant = false, quotesRelevanceDuration, TimeUnit.MINUTES);
        }
    }

    // Возвращает текст сообщения с котировками, которое будет отправлено пользователям
    public String getFiatCurrenciesQuotesMessage() {
        String quotesUpdateTime = USD_RUB.getQuotesUpdateTime(); // получаем время последнего обновления котировок

        // формируем текст сообщения для отправки пользователям
        String messageHeader = "Курсы валют на " + quotesUpdateTime + " по мск:\n";
        String messageBody = buildMessageBody(fiatCurrencyQuotes);
        return messageHeader + messageBody;
    }

    // Возвращает текст сообщения с котировками криптовалют, которое будет отправлено пользователям
    public String getCryptocurrenciesQuotesMessage() {
        // формируем текст сообщения с котировками криптовалют для отправки пользователям
        String messageHeader = "Котировки криптовалют на бирже Bybit:\n";
        String messageBody = buildMessageBody(cryptoCurrencyQuotes);
        return messageHeader + messageBody;
    }

    // формирует тело текста (без заголовка) сообщения с котировками, которое будет отправлено пользователям
    private String buildMessageBody(Map<CurrencyPair, Double> quotes) {
        StringBuilder messageBuilder = new StringBuilder();
        quotes.forEach((currencyPair, quote) -> {
            String firstCurrency = currencyPair.getFirstCurrency().getCode();
            String quoteStr = Utilities.formatDouble(quote);
            String secondCurrency = currencyPair.getSecondCurrency().getCode();

            // метод String.format() заполняет шаблон строки (первый аргумент) строковыми вставками (последующие аргументы)
            String currencyPairMessage = String.format("\n1 %s = %s %s", firstCurrency, quoteStr, secondCurrency);
            messageBuilder.append(currencyPairMessage);
        });

        return messageBuilder.toString();
    }
}