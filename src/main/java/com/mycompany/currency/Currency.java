package com.mycompany.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.mycompany.currency.Currency.CurrencyType.*;

/**
 * Валюта
 */
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
    LINEA("LINEA", "Linea", CRYPTO),
    USDT("USDT", "USD Tether", CRYPTO);

    //нужно ли это поле ? почти все валюты
    private final String code; // буквенное или символьное обозначение валюты
    private final String shortName; // краткое название
    private final CurrencyType type; // тип валюты

    // тип валюты
    public enum CurrencyType {
        FIAT, // фиатная валюта
        CRYPTO // криптовалюта
    }
}