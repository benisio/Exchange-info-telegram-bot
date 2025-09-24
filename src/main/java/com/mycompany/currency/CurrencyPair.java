package com.mycompany.currency;

import static com.mycompany.currency.Currency.CurrencyType.*;
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
     * Возвращает базовую (первую из двух) валюту данной валютной пары
     */
    Currency getFirstCurrency();

    /**
     * Возвращает вторую валюту данной валютной пары
     */
    Currency getSecondCurrency();

    /**
     * Возвращает краткое название данной валютной пары
     */
    String getShortName();

    /**
     * Возвращает true, если данная валютная пара является парой фиатных валют, иначе false.
     */
    default boolean isFiat() {
        return getFirstCurrency().getType() == FIAT && getSecondCurrency().getType() == FIAT;
    }

    /**
     * Возвращает true, если данная валютная пара является криптовалютной парой, иначе false.
     */
    default boolean isCrypto() {
        return getFirstCurrency().getType() == CRYPTO && getSecondCurrency().getType() == CRYPTO;
    }

    /**
     *
     */
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