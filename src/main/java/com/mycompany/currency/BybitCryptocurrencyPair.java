package com.mycompany.currency;

import com.mycompany.HttpRequestSender;
import com.mycompany.Utilities;
import com.mycompany.json.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Криптовалютная пара, торгующаяся на бирже Bybit.
 */
@Getter
@AllArgsConstructor
public enum BybitCryptocurrencyPair implements CurrencyPair {

    BTC_USDT("BTCUSDT", "BTC", "USDT"), // Bitcoin к USDT
    ETH_USDT("ETHUSDT", "ETH", "USDT"), // Etherium к USDT
    SOL_USDT("SOLUSDT", "SOL", "USDT"), // Solana к USDT
    WLKN_USDT("WLKNUSDT", "WLKN", "USDT"); // Walken к USDT

    private final String ticker; // тикер данной валютной пары на бирже Bybit
    private final String firstCurrencyCode; // код базовой (первой из двух) валюты
    private final String secondCurrencyCode; // код второй валюты

    @Override
    public double getQuote() {
        return getLastMarketPrice();
    }

    private Double getLastMarketPrice() {
        Map<String, String> currencyPairData = getMarketDataFromBybit();
        String lastPriceStr = currencyPairData.get("lastPrice");
        return Double.parseDouble(lastPriceStr);
    }

    private Map<String, String> getMarketDataFromBybit() {
        String bybitMarketDataJsonResponse = Utilities.readResponse(HttpRequestSender.newBybitMarketDataRequest(this));
        return JsonReader.parseBybitMarketDataToMap(bybitMarketDataJsonResponse);
    }

}