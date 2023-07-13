package com.mycompany.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.mycompany.currency.Currency.CurrencyType.*;

@Getter
@AllArgsConstructor
public enum Currency {

    USD("$", "доллар", FIAT),
    EUR("€", "евро", FIAT),
    RUB("RUB", "рубль", FIAT),
    KZT("KZT", "тенге", FIAT),
    CNY("CNY", "юань", FIAT),
    TRY("TRY", "лира", FIAT),
    BYN("BYN", "рубль РБ", FIAT),

    BTC("BTC", "Bitcoin", CRYPTO),
    ETH("ETH", "Etherium", CRYPTO),
    SOL("SOL", "Solana", CRYPTO),
    WLKN("WLKN", "Walken", CRYPTO),
    USDT("USDT", "USD Tether", CRYPTO);

    private final String code; // нужно ли это поле ? почти все валюты
    private final String shortName;
    private final CurrencyType type;

    public enum CurrencyType {
        FIAT,
        CRYPTO;
    }
}