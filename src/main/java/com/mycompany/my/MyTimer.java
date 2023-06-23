package com.mycompany.my;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Моя реализация класса java.util.Timer с помощью паттерна "делегирование".
 *
 * Timer и TimerTask - это служебные классы Java, используемые для планирования задач в фоновом потоке.
 * В двух словах: TimerTask is the task to perform and Timer is the scheduler.
 *
 * Что мне не нравится в оригинальной реализации
 * Рассмотрим параметры методов schedule(TimerTask task, Date firstTime, long period),
 *                              schedule(TimerTask task, long delay):
 * TimerTask - это абстрактный класс, а не функциональный интерфейс. Поэтому его нельзя реализовать с помощью лямбды,
 * обязательно нужно юзать анонимный класс. Из-за этого теряем в читабельности кода.
 * Date - устаревший неудобный класс, гораздо удобнее использовать современные классы из пакета java.time, например, ZonedDateTime.
 * long period, long delay - только в мс, а хотелось бы еще секунды, минуты, часы, дни
 *
 * Коммент на StackOverflow: Technically your answer is correct, however I will personally go
 * with ScheduledExecutorService instead of TimerTask.
 * <a href="https://stackoverflow.com/questions/22378422/how-to-use-timertask-with-lambdas">...</a>
 * */
public class MyTimer {

    private Timer timer;

    public MyTimer() {
        timer = new Timer();
    }

    // заменяет метод Timer#schedule(TimerTask task, Date firstTime, long period)
    public void schedule(MyTimerTask timerTask, ZonedDateTime firstExecTime, long period, TimeUnit unit) {
        Date date = Date.from(firstExecTime.toInstant());
        long periodMillis = unit.toMillis(period); // переводим period и unit в мс
        timer.schedule(convert(timerTask), date, periodMillis);
    }

    // заменяет метод Timer#schedule(TimerTask task, long delay)
    public void schedule(MyTimerTask timerTask, long delay, TimeUnit unit) {
        long delayMillis = unit.toMillis(delay); // переводим delay и unit в мс
        timer.schedule(convert(timerTask), delayMillis);
    }

    // конвертирует MyTimerTask в TimerTask
    private TimerTask convert(MyTimerTask timerTask) {
        return new TimerTask() {
            @Override
            public void run() {
                timerTask.run();
            }
        };
    }
}