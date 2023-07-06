package com.mycompany.currency;

/**
 * Валютная пара
 */
public interface CurrencyPair extends EnumInterface {

    /**
     * Возвращает тикер данной валютной пары
     */
    default String getTicker() {
        return "";
    }

    /**
     * Возвращает котировку данной валютной пары
     */
    double getQuote();

    /**
     * Возвращает код базовой (первой из двух) валюты
     */
    Currency getFirstCurrency();

    /**
     * Возвращает код второй валюты
     */
    Currency getSecondCurrency();

    /**
     *
     */
    String getShortName();

    /**
     *
     */
    default boolean isCrypto() {
        return false;
    }

    /**
     *
     */
    default boolean isFiat() {
        return false;
    }

    static CurrencyPair valueOf(String name) {
        try {
            return MoexCurrencyPair.valueOf(name);
        } catch (IllegalArgumentException e) {
            try {
                return BybitCryptocurrencyPair.valueOf(name);
            } catch (IllegalArgumentException e1) {
                try {
                    return CalculatedQuoteCurrencyPair.valueOf(name);
                } catch (IllegalArgumentException e2) {
                    e.printStackTrace();
                }
            }
        }
        throw new IllegalArgumentException();
    }
}