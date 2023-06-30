package com.mycompany;

import com.mycompany.currency.CurrencyPair;
import com.mycompany.currency.MoexCurrencyPair;
import com.mycompany.currency.BybitCryptocurrencyPair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.time.LocalDate;

/**
 * Класс, содержащий статичные методы, создающие различные GET-запросы к биржам.
 */
public class HttpRequestFactory {

    /**
     * Запрещаем создавать экземпляры класса, так как он состоит только из статичных методов.
     */
    private HttpRequestFactory() {}

    /**
     * Отправляет http-запрос к API Мосбиржи (ISS MOEX API) на получение биржевых данных о торгах данной
     * валютной пары за текущую или последнюю (если текущая уже закончилась) торговую сессию. Пример ответа на такой
     * запрос приведен в файле: src/example/moex_usd_rub_marketdata.json.
     * @param currencyPair - валютная пара, для которой отправляем запрос
     */
    public static String newMoexLastTradingDayDataRequest(MoexCurrencyPair currencyPair) {
        return newGetRequest(currencyPair, null, null);
    }

    /**
     * Отправляет http-запрос к API Мосбиржи (ISS MOEX API) на получение биржевых данных о торгах данной
     * валютной пары за предыдущие торговые сессии в указанном диапазоне дат. Пример ответа на такой запрос приведен
     * в файле: src/example/moex_history_data.json.
     * @param currencyPair - валютная пара, для которой отправляем запрос
     * @param fromDate - начальная дата диапазона дат, за который хотим получить данные о торгах
     * @param tillDate - конечная дата диапазона дат, за который хотим получить данные о торгах (что если сюда передать сегодняшнюю дату ???)
     */
    public static String newMoexPreviousDaysDataRequest(MoexCurrencyPair currencyPair, LocalDate fromDate, LocalDate tillDate) {
        return newGetRequest(currencyPair, fromDate, tillDate);
    }

    /** Отправляет http-запрос к API биржи Bybit на получение биржевых данных о торгах данной
     * валютной пары за текущую торговую сессию. Пример ответа на такой запрос приведен в файле:
     * src/example/bybit_wlkn_usdt_marketdata.json.
     * @param currencyPair - криптовалютная пара на бирже Bybit
     */
    public static String newBybitMarketDataRequest(BybitCryptocurrencyPair currencyPair) {
        return newGetRequest(currencyPair, null, null);
    }

    // шаблонный метод для отправки http-запроса с помощью Apache Http Client
    private static String newGetRequest(CurrencyPair currencyPair, LocalDate fromDate, LocalDate tillDate) {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpUriRequest httpGet = new HttpGet(getRequestUri(currencyPair, fromDate, tillDate));
        try (CloseableHttpResponse httpResponse = httpclient.execute(httpGet)) {
            final HttpEntity responseEntity = httpResponse.getEntity();
            String response = EntityUtils.toString(responseEntity);
            httpclient.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    // возвращает URL запроса в зависимости от параметров
    private static String getRequestUri(CurrencyPair currencyPair, LocalDate fromDate, LocalDate tillDate) {
        String ticker = currencyPair.getTicker();
        if (currencyPair instanceof MoexCurrencyPair) {
            if (fromDate != null && tillDate != null) {
                return  "https://iss.moex.com/iss/history/engines/currency/markets/selt/boards/CETS/securities/" +
                        ticker + ".json?iss.meta=off&from=" + fromDate + "&till=" + tillDate + "&sort_order=desc";
            } else {
                return  "https://iss.moex.com/iss/engines/currency/markets/selt/boards/CETS/securities/"
                        + ticker + ".json?iss.meta=off&iss.only=marketdata";
            }
        } else if (currencyPair instanceof BybitCryptocurrencyPair) {
            return  "https://api.bybit.com/v5/market/tickers?category=spot&symbol=" + ticker;
        }

        throw new IllegalArgumentException();
    }
}