package com.mycompany;

import com.mycompany.currency.MoexCurrencyPair;
import com.mycompany.currency.BybitCryptocurrencyPair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

/**
 * Класс, содержащий статичные методы, создающие различные GET-запросы к API Мосбиржи (ISS MOEX API).
 */
public class HttpRequestSender {

    /**
     * Запрещаем создавать экземпляры класса, так как он состоит только из статичных методов.
     */
    private HttpRequestSender() {}

    /**
     * Отправляет http-запрос к API Мосбиржи (ISS MOEX API) на получение биржевых данных о торгах данной
     * валютной пары за текущую или последнюю (если текущая уже закончилась) торговую сессию. Пример ответа на такой
     * запрос приведен в файле: /src/example/moex_usd_rub_marketdata.json.
     * @param currencyPair - валютная пара, для которой отправляем запрос
     */
    public static HttpURLConnection newLastTradingDayDataRequest(MoexCurrencyPair currencyPair) {
        final String ticker = currencyPair.getTicker();
        final URL url;
        final HttpURLConnection connection;
        try {
            url = new URL("https://iss.moex.com/iss/engines/currency/markets/selt/boards/CETS/securities/"
                    + ticker + ".json?iss.meta=off&iss.only=marketdata");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000); //
            connection.setReadTimeout(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }

    /**
     * Отправляет http-запрос к API Мосбиржи (ISS MOEX API) на получение биржевых данных о торгах данной
     * валютной пары за предыдущие торговые сессии в указанном диапазоне дат. Пример ответа на такой запрос приведен
     * в файле: /src/example/moex_history_data.json.
     * @param currencyPair - валютная пара, для которой отправляем запрос
     * @param fromDate - начальная дата диапазона дат, за который хотим получить данные о торгах
     * @param tillDate - конечная дата диапазона дат, за который хотим получить данные о торгах (что если сюда передать сегодняшнюю дату ???)
     */
    public static HttpURLConnection newPreviousDaysDataRequest(MoexCurrencyPair currencyPair, LocalDate fromDate, LocalDate tillDate) {
        final String ticker = currencyPair.getTicker();
        final URL url;
        final HttpURLConnection connection;
        try {
            // дата в запросе должна отображаться в формате 2023-06-13, класс LocalDate при вызове toString()
            // автоматически приводит дату к такому формату
            url = new URL("https://iss.moex.com/iss/history/engines/currency/markets/selt/boards/CETS/securities/" +
                    ticker + ".json?iss.meta=off&from=" + fromDate + "&till=" + tillDate + "&sort_order=desc");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000); //
            connection.setReadTimeout(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }

    /** Отправляет http-запрос к API биржи Bybit на получение биржевых данных о торгах данной
     * валютной пары за текущую торговую сессию.
     * @param currencyPair - валютная пара на бирже Bybit
     */
    public static HttpURLConnection newBybitMarketDataRequest(BybitCryptocurrencyPair currencyPair) {
        String ticker = currencyPair.getTicker();
        final URL url;
        final HttpURLConnection connection;
        try {
            url = new URL("https://api.bybit.com/v5/market/tickers?category=spot&symbol=" + ticker);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000); //
            connection.setReadTimeout(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}