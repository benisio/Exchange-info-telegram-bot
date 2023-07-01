package com.mycompany.currency;

/**
 * Валютная пара
 */
public interface CurrencyPair {

    /**
     * Возвращает тикер данной валютной пары
     */
    String getTicker();

    /**
     * Возвращает котировку данной валютной пары
     */
    double getQuote();

    /**
     * Возвращает код базовой (первой из двух) валюты
     */
    String getFirstCurrencyCode();

    /**
     * Возвращает код второй валюты
     */
    String getSecondCurrencyCode();
}