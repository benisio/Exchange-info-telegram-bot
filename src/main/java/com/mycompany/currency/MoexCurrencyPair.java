package com.mycompany.currency;

import com.mycompany.HttpRequestFactory;
import com.mycompany.json.JsonReader;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Валютная пара, торгующаяся на Московской бирже (Мосбиржа, MOEX).
 */
public enum MoexCurrencyPair implements CurrencyPair {

    // валютные пары с рублем
    USD_RUB("USD000UTSTOM", "$", "RUB", 1), // доллар США к рублю
    EUR_RUB("EUR_RUB__TOM", "€", "RUB", 1), // евро к рублю
    CNY_RUB("CNYRUB_TOM", "CNY", "RUB", 1), // китайский юань к рублю
    // котировку KZT_RUB не будем рассылать пользователям, но она нужна для расчета котировки RUB/KZT, см. класс CalculatedQuoteCurrencyPair
    KZT_RUB("KZTRUB_TOM", "KZT", "RUB", 100), // казахстанский тенге к рублю
    TRY_RUB("TRYRUB_TOM", "TRY", "RUB", 1), // турецкая лира к рублю

    // валютные пары с долларом
    EUR_USD("EURUSD000TOM", "€", "$", 1), // евро к доллару США
    USD_KZT("USDKZT_TOM", "$", "KZT", 1); // доллар США к казахстанскому тенге


    @Getter private final String ticker; // тикер данной валютной пары на Мосбирже
    @Getter private final String firstCurrencyCode; // код базовой (первой из двух) валюты
    @Getter private final String secondCurrencyCode; // код второй валюты

    // Количество валюты, за которое указывается курс в котировках.
    // Например, если для пары KZT/RUB значение FACEVALUE = 100, а LAST = 19, то это означает, что 100 KZT = 19 RUB.
    // Для упрощения кода вбиваю это значение ручками в конструкторе для каждой валютной пары, значения взял из
    // json-ответов API Мосбиржи на запросы описания инструмента для каждой конкретной валютной пары.
    // Пример такого запроса: https://iss.moex.com/iss/securities/USD000UTSTOM.xml?iss.meta=off.
    // Пользователям будем отправлять котировки в пересчете на 1 ед. валюты, поэтому все котировки, получаемые на
    // Мосбирже, делим на faceValue (см. метод getQuote()).
    @Getter private final int faceValue;

    // мапы для хранения распарсенных данных из json-ответов API Мосбиржи на наши запросы по данной валютной паре
    private Map<String, String> lastTradingDayData; // мб убрат эти поля и сделат их локалными переменными ?
    private Map<String, String> previousDayData;

    /**
     * Конструктор
     *
     * @param ticker             тикер валютной пары
     * @param firstCurrencyCode  код базовой (первой) валюты
     * @param secondCurrencyCode код второй валюты
     * @param faceValue          количество валюты лота, за которое указывается курс в котировках
     */
    private MoexCurrencyPair(String ticker, String firstCurrencyCode, String secondCurrencyCode, int faceValue) {
        this.ticker = ticker;
        this.firstCurrencyCode = firstCurrencyCode;
        this.secondCurrencyCode = secondCurrencyCode;
        this.faceValue = faceValue;
    }

    /**
     * Возвращает котировку данной валютной пары в пересчете на 1 ед. базовой (первой из двух) валюты.
     * Для получения такой котировки все получаемые на Мосбирже котировки делим на faceValue.
     */
    @Override
    public double getQuote() {
        getMarketDataFromMoex();

        double quote;
        Optional<Double> optLastMarketPrice = getLastMarketPrice();
        if (optLastMarketPrice.isPresent()) {
            quote = optLastMarketPrice.get()/faceValue;
        } else {
            double closePrice = getPreviousDayClosePrice();
            quote = closePrice/faceValue;
        }
        return quote;
    }

    // загружает в мапы данные Мосбиржи о текущей/последней и о предыдущей торговой сессии
    private void getMarketDataFromMoex() {
        lastTradingDayData = getLastTradingDayDataFromMoex();
        previousDayData = getPreviousDayDataFromMoex();
    }

    /**
     * Возвращает время последнего обновления котировок (свойство UPDATETIME из json-ответа Мосбиржи) за текущую или
     * последнюю (если текущая уже завершилась) торговую сессию данной валютной пары. Формат возвращаемого
     * времени: HH:mm. Пример такого json-ответа приведен в файле: src/example/moex_usd_rub_marketdata.json.
     * При вызове данного метода в середине торговой сессии, как правило, это время отстает от фактического примерно
     * на 15 мин - таково ограничение Мосбиржи на использование её API на бесплатной основе. При вызове данного метода
     * после окончания торговой сессии (при условии, что следующая торговая сессия еще не началась), возвращает время
     * окончания торговой сессии.
     *
     * @see #getLastMarketPrice()
     * @return String с временем
     */
    public String getQuotesUpdateTime() {
        // получаем на Мосбирже время последнего обновления в формате "HH:mm:ss"
        var parseFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String quotesUpdateTimeStr = getValueFromLastTradingDayData("UPDATETIME");
        var quotesUpdateTime = LocalTime.parse(quotesUpdateTimeStr, parseFormatter);

        // форматируем полученное время - убираем секунды
        var timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return timeFormatter.format(quotesUpdateTime);
    }

    /**
     * Возвращает цену последней сделки (котировка LAST из json-ответа Мосбиржи) за текущую или последнюю (если текущая
     * уже завершилась) торговую сессию по данной валютной паре. Пример такого json-ответа приведен в файле:
     * src/example/moex_usd_rub_marketdata.json
     * Данная котировка актуальна на момент времени, указанный в свойстве "UPDATETIME" json-ответа. Также это время
     * возвращает метод getQuotesUpdateTime().
     *
     * @see #getQuotesUpdateTime()
     * @return Optional, содержащий одно из двух: цену последней сделки или null
     */
    private Optional<Double> getLastMarketPrice() {
        Optional<String> optLastMarketPrice = Optional.ofNullable(getValueFromLastTradingDayData("LAST"));
        if (optLastMarketPrice.isPresent()) {
            double lastMarketPrice = Double.parseDouble(optLastMarketPrice.get());
            return Optional.of(lastMarketPrice);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Возвращает цену закрытия (котировку CLOSE из json-ответа) предыдущей торговой сессии данной валютной пары.
     * Пример такого json-ответа приведен в файле: src/example/moex_history_data.json
     */
    private double getPreviousDayClosePrice() {
        return Double.parseDouble(getValueFromPreviousDayData("CLOSE"));
    }

    /**
     * Возвращает значение по названию свойства из json-ответа Мосбиржи по текущей или последней (если текущая уже
     * завершилась) торговую сессию данной валютной пары.
     * @param jsonProperty - свойство из json-ответа Мосбиржи, для которого хотим получить значение
     * @return String со значением свойства
     */
    private String getValueFromLastTradingDayData(String jsonProperty) {
        return lastTradingDayData.get(jsonProperty);
    }

    /**
     * Возвращает значение по названию свойства из json-ответа Мосбиржи по предыдущей торговой сессии данной валютной
     * пары.
     * @param jsonProperty - свойство из json-ответа Мосбиржи, для которого хотим получить значение
     * @return String со значением свойства
     */
    private String getValueFromPreviousDayData(String jsonProperty) {
        return previousDayData.get(jsonProperty);
    }


    /**
     * Возвращает map с данными Мосбиржи о текущей или последней (если текущая уже завершилась) торговой сессии данной
     * валютной пары.
     */
    private Map<String, String> getLastTradingDayDataFromMoex() {
        String marketDataJsonResponse = HttpRequestFactory.newMoexLastTradingDayDataRequest(this);
        return JsonReader.parseLastTradingDayDataToMap(marketDataJsonResponse);
    }

    /**
     * Возвращает map с данными Мосбиржи о предыдущей торговой сессии данной валютной пары.
     */
    private Map<String, String> getPreviousDayDataFromMoex() {
        // Бывают дни (выходные и праздники, в том числе длинные), когда биржа не работает, и, соответственно, данных
        // биржи за эти дни нет. Поэтому для получения данных по предыдущей торговой сессии на всякий случай получаем
        // результаты торгов за последнюю неделю, так как в любые 7 дней в году наверняка попадет хотя бы 1 рабочий
        // день биржи.
        ZonedDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        LocalDate fromDate = nowInMoscow.minusDays(7).toLocalDate(); // день неделю назад
        LocalDate tillDate = nowInMoscow.minusDays(1).toLocalDate(); // вчерашний день

        // отправляем запрос, читаем ответ
        String lastWeekDataJsonResponse = HttpRequestFactory.newMoexPreviousDaysDataRequest(this, fromDate, tillDate);

        // из данных за неделю получаем данные за предыдущую торговую сессию и парсим их в map
        return JsonReader.parsePreviousDayDataToMap(lastWeekDataJsonResponse);
    }
}