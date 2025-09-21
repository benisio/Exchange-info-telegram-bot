package com.mycompany.currency;

import com.mycompany.HttpRequestFactory;
import com.mycompany.json.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

import static com.mycompany.currency.Currency.*;

/**
 * Криптовалютная пара, торгующаяся на бирже Bybit.
 */
@Getter
@AllArgsConstructor
public enum BybitCryptocurrencyPair implements CurrencyPair {

    BTC_USDT("BTCUSDT", BTC, USDT), // Bitcoin к USDT
    ETH_USDT("ETHUSDT", ETH, USDT), // Etherium к USDT
    SOL_USDT("SOLUSDT", SOL, USDT), // Solana к USDT
    WLKN_USDT("WLKNUSDT", WLKN, USDT); // Walken к USDT

    private final String ticker; // тикер данной валютной пары на бирже Bybit
    private final Currency firstCurrency; // код базовой (первой из двух) валюты
    private final Currency secondCurrency; // код второй валюты

    /**
     * Возвращает котировку данной криптовалютной пары.
     */
    @Override
    public double getQuote() {
        return getLastMarketPrice();
    }

    @Override
    public String getShortName() {
        return this.name();
    }

    /**
     * Возвращает цену последней сделки (котировка "lastPrice" из json-ответа биржи Bybit) за текущую торговую сессию
     * по данной валютной паре. Пример такого json-ответа приведен в файле:
     * src/example/bybit_wlkn_usdt_marketdata.json
     */
    private Double getLastMarketPrice() {
        Map<String, String> currencyPairData = getMarketDataFromBybit();
        String lastPriceStr = currencyPairData.get("lastPrice");
        return Double.parseDouble(lastPriceStr);
    }

    /**
     * Возвращает Map с биржевыми данными торгов по данной криптовалютной паре.
     */
    private Map<String, String> getMarketDataFromBybit() {
        String bybitMarketDataJsonResponse = HttpRequestFactory.newBybitMarketDataRequest(this);
        return JsonReader.parseBybitMarketDataToMap(bybitMarketDataJsonResponse);
    }

}