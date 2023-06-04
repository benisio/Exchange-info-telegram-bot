package com.mycompany;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javax.annotation.processing.Generated;
import java.util.List;

/**
 * POJO-классы для парсинга json в объекты Java. Повторяют структуру json-ответа на наш http-запрос
 * к API Мосбиржи. Пример такого ответа приведен в файле /src/example/response.json.
 *
 * Код данных двух классов сгенерирован с помощью сервиса https://www.jsonschema2pojo.org/
 * */
@Generated("jsonschema2pojo")
public class ExchangeData {

        @SerializedName("marketdata")
        @Expose
        public MarketData marketdata;

        public MarketData getMarketdata() {
            return marketdata;
        }
}

@Generated("jsonschema2pojo")
class MarketData {

        @SerializedName("columns")
        @Expose
        public List<String> columns;
        @SerializedName("data")
        @Expose
        public List<List<String>> data;

        public List<String> getColumns() {
                return columns;
        }

        public List<String> getData() {
            return data.get(0);
        }
}