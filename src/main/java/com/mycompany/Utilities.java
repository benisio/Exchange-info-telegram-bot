package com.mycompany;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Утилитный класс с утилитными методами.
 */
public final class Utilities {

    /**
     * Запрещаем создавать экземпляры класса, так как он состоит только из статичных методов.
     */
    private Utilities() {}

    // выводит содержимое map в консоль
    public static <K, V> void printMap(Map<K, V> map) {
        map.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    // округляет число типа double до 2 знаков после запятой и конвертирует в String
    public static String formatDouble(Double d) {
        return new DecimalFormat("#.##").format(d); // эта реализация меняет десятичную точку на запятую и убирает незначащие нули
        //return String.format("%.2f", d); // эта реализация меняет десятичную точку на запятую и НЕ убирает незначащие нули
    }

    // объединяет два списка в Map
    public static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        if (keys.size() == values.size()) {
            Map<K, V> map = new LinkedHashMap<>(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }
            return map;
        } else {
            throw new IllegalArgumentException("списки должны иметь одинаковые размеры");
        }
    }

    /**
     * Читает и возвращает ответ на http-запрос
     * @param connection - соединение, соответствующее http-запросу
     */
    public static String readResponse(HttpURLConnection connection) {
        final StringBuilder content = new StringBuilder();

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return content.toString();
    }
}