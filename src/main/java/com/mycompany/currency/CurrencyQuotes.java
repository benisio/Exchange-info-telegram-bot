package com.mycompany.currency;

import com.mycompany.Utilities;
import com.mycompany.my.MyTimer;
import com.mycompany.service.KucoinMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mycompany.currency.MoexCurrencyPair.*;
import static com.mycompany.currency.KucoinCryptocurrencyPair.*;
import static com.mycompany.currency.CalculatedQuoteCurrencyPair.*;

/**
 * Класс, хранящий в себе Map с котировками.
 * Оформил в виде отдельного класса, делегирующий свое поведение объекту Map, так как стандартного функционала Map
 * недостаточно, нужны дополнительные методы.
 */
@RequiredArgsConstructor
@Component
public class CurrencyQuotes {

  public static final List<CurrencyPair> FIAT_CURRENCY_PAIRS = List.of(
      USD_RUB,
      EUR_RUB,
      CNY_RUB,
      TRY_RUB,
      EUR_USD,
      USD_KZT,
      RUB_KZT,
      USD_BYN
  );

  public static final List<CurrencyPair> CRYPTO_CURRENCY_PAIRS = List.of(KucoinCryptocurrencyPair.values());

  public static final List<CurrencyPair> ALL_CURRENCY_PAIRS =
      Stream.of(FIAT_CURRENCY_PAIRS, CRYPTO_CURRENCY_PAIRS)
      .flatMap(List::stream)
      .toList();

    // заводим отдельные мапы для крипты и фиатных валют, так как будем отправлять их котировки в разных сообщениях
    private Map<CurrencyPair, Double> fiatCurrencyQuotes;
    private Map<CurrencyPair, Double> cryptoCurrencyQuotes;
  private final KucoinMarketService kucoinMarketService;

    // Флаг актуальности котировки.
    // Нужен для того, чтобы при обращении к боту одновременно 100 юзеров, бот не отправлял одновременно 100 запросов к
    // бирже, а обратился к ней 1 раз, сохранил у себя в поле currencyQuotes полученные котировки на время, равное
    // длительности актуальности котировки, и при обращениях юзеров в течение этого времени возвращал бы им эти
    // сохраненные котировки.
    private volatile boolean quotesRelevant;

    // получает актуальные котировки для каждой валютной пары
    public void getRelevantQuotes() { // метод называется get..., но возвращает void. Обычно геттеры возвращают значение. Корректный ли здесь нейминг ?
        if (!quotesRelevant) { // если котировки неактуальны, получаем актуальные и кладем в mapы
            fiatCurrencyQuotes = getQuotes(FIAT_CURRENCY_PAIRS); // фиатные валюты
            cryptoCurrencyQuotes = getQuotes(CRYPTO_CURRENCY_PAIRS); // крипта
            quotesRelevant = true; // устанавливаем флаг актуальности котировок

            // длительность актуальности котировок
            final int quotesRelevanceDuration = 5; // в минутах
            // через 5 мин (длительность актуальности котировок, quotesRelevanceDuration) сбрасываем флаг актуальности
            new MyTimer().schedule(() -> quotesRelevant = false, quotesRelevanceDuration, TimeUnit.MINUTES);
        }
    }

    // Возвращает текст сообщения с котировками, которое будет отправлено пользователям
    public String getFiatCurrenciesQuotesMessage(Set<String> displayableCurrencies) {
        String quotesUpdateTime = USD_RUB.getQuotesUpdateTime(); // получаем время последнего обновления котировок

        // формируем текст сообщения для отправки пользователям
        String messageHeader = "Курсы валют на " + quotesUpdateTime + " по мск:\n";
        String messageBody = buildMessageBody(fiatCurrencyQuotes, displayableCurrencies);
        return messageHeader + messageBody;
    }

    // Возвращает текст сообщения с котировками криптовалют, которое будет отправлено пользователям
    public String getCryptocurrenciesQuotesMessage(Set<String> displayableCurrencies) {
        // формируем текст сообщения с котировками криптовалют для отправки пользователям
        String messageHeader = "Котировки криптовалют на бирже Bybit:\n";
        String messageBody = buildMessageBody(cryptoCurrencyQuotes, displayableCurrencies);
        return messageHeader + messageBody;
    }

    // формирует тело текста (без заголовка) сообщения с котировками, которое будет отправлено пользователям
    private String buildMessageBody(Map<CurrencyPair, Double> quotes, Set<String> displayableCurrencies) {
        //StringBuilder messageBuilder = new StringBuilder();
        return quotes.entrySet().stream()
            .filter(entry -> displayableCurrencies.contains(entry.getKey().name()))
            .map(entry -> {
              CurrencyPair currencyPair = entry.getKey();
              Double quote = entry.getValue();
              String firstCurrency = currencyPair.getFirstCurrency().getCode();
              String quoteStr = Utilities.formatDouble(quote);
              String secondCurrency = currencyPair.getSecondCurrency().getCode();

              // метод String.format() заполняет шаблон строки (первый аргумент) строковыми вставками (последующие аргументы)
              return String.format("\n1 %s = %s %s", firstCurrency, quoteStr, secondCurrency);
              //messageBuilder.append(currencyPairMessage);
        }).collect(Collectors.joining());

        //return messageBuilder.toString();
    }

    private Map<CurrencyPair, Double> getQuotes(List<CurrencyPair> currencyPairs) {
        Map<CurrencyPair, Double> quotes = new LinkedHashMap<>();
        for (CurrencyPair pair : currencyPairs) {
          if (pair instanceof KucoinCryptocurrencyPair kucoinCryptocurrencyPair) {
            quotes.put(pair, kucoinMarketService.getQuote(kucoinCryptocurrencyPair));
          } else {
            quotes.put(pair, pair.getQuote());
          }
        }

        return quotes;
    }
}