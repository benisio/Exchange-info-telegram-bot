package com.mycompany.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import static com.mycompany.currency.MoexCurrencyPair.*;

/**
 * Валютная пара, не торгующаяся на бирже, котировка которой может быть рассчитана на основе котировки валютной
 * пары, торгующейся на бирже.
 *
 * Необходимость в этом enum-классе присутствует потому, что котировки описанных в нем валютных пар нужны для
 * отображения в боте, но получить их на бирже нельзя, можно только рассчитать.
 *
 * К примеру, на Мосбирже котируется валютная пара KZT_RUB. Однако в жизни гораздо чаще применяется котировка RUB_KZT
 * из-за ее удобства в расчетах по сравнению с KZT_RUB. Сравним котировки на конец 16 июня 2023 г.:
 * RUB_KZT: 1 RUB = 5.31 KZT - удобно умножать и делить на ~5.3 при пересчете из KZT в RUB или обратно
 * KZT_RUB: 1 KZT = 0.188 RUB - не удобно умножать и делить на 0.188 при пересчете из KZT в RUB или обратно
 * Поэтому гораздо логичнее отправлять пользователям рассчитанную котировку пары RUB_KZT, а не взятую на Мосбирже
 * котировку пары KZT_RUB.
 */
@Getter
@AllArgsConstructor
public enum CalculatedQuoteCurrencyPair implements CurrencyPair {

    RUB_KZT(1 / KZT_RUB.getQuote() ,"RUB","KZT"); // рубль к казахстанскому тенге

    private final double quote; // рассчитанная котировка данной валютной пары
    private final String firstCurrencyCode; // код базовой (первой) валюты
    private final String secondCurrencyCode; // код второй валюты
}