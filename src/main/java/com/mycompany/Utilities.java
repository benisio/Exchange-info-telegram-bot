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

    /**
     * Выводит содержимое Map в консоль.
     */
    public static <K, V> void printMap(Map<K, V> map) {
        map.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    /**
     * Округляет число типа double до 2 или 3 знаков после запятой в зависимости от величины этого числа, и конвертирует
     * его в String.
     */
    public static String formatDouble(Double d) {
        double threshold = 1.5d; // пороговое значение

        // если число больше порогового значения, округляем его до 2-х знаков после запятой, если меньше - до 3-х
        if (d >= threshold) {
            return new DecimalFormat("#.##").format(d); // эта реализация меняет десятичную точку на запятую и убирает незначащие нули
        } else {
            return new DecimalFormat("#.###").format(d);
        }

        //return String.format("%.2f", d); // эта реализация меняет десятичную точку на запятую и НЕ убирает незначащие нули
    }

    /**
     * Объединяет два списка в Map.
     * @param keys - список, элементы которого станут ключами Map
     * @param values - список, элементы которого станут значениями Map
     * @return результирующую Map, в которой ключи - элементы первого списка, а значения - элементы второго списка
     * @throws IllegalArgumentException if lists sizes aren't equal
     */
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
}