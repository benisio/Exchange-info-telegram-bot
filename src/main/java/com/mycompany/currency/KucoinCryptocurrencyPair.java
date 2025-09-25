package com.mycompany.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.mycompany.currency.Currency.BTC;
import static com.mycompany.currency.Currency.ETH;
import static com.mycompany.currency.Currency.LINEA;
import static com.mycompany.currency.Currency.SOL;
import static com.mycompany.currency.Currency.USDT;

/**
 * Криптовалютная пара, торгующаяся на бирже Kucoin.
 */
@Getter
@AllArgsConstructor
public enum KucoinCryptocurrencyPair implements CurrencyPair {

    BTC_USDT("BTC-USDT", BTC, USDT),
    ETH_USDT("ETH-USDT", ETH, USDT),
    SOL_USDT("SOL-USDT", SOL, USDT),
    LINEA_USDT("LINEA-USDT", LINEA, USDT);

    private final String ticker; // тикер данной криптовалютной пары на бирже Bybit
    private final Currency firstCurrency; // базовая валюта (первая из двух)
    private final Currency secondCurrency; // вторая валюта

    /**
     * Возвращает котировку данной криптовалютной пары.
     */
    @Override
    public double getQuote() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getShortName() {
        return this.name();
    }

}