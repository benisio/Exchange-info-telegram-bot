package com.mycompany.my;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Моя замена классу java.util.Timer, реализованная с помощью паттерна "делегирование".
 *
 * Timer и TimerTask (Java 3) - это служебные классы Java, используемые для планирования задач в фоновом потоке.
 * В двух словах: TimerTask is the task to perform and Timer is the scheduler.
 *
 * Что мне не нравится в оригинальной реализации:
 * Рассмотрим параметры методов schedule(TimerTask task, Date firstTime, long period),
 *                              schedule(TimerTask task, long delay):
 * TimerTask - это абстрактный класс, а не функциональный интерфейс. Поэтому его нельзя реализовать с помощью
 * лямбда-выражения, обязательно нужно использовать анонимный класс. Из-за этого теряем в читабельности кода.
 * Date - устаревший неудобный класс, гораздо удобнее использовать более современные классы из пакета java.time,
 * например, ZonedDateTime.
 * long period, long delay - только в мс, а хотелось бы еще секунды, минуты, часы, дни.
 *
 * Коммент на StackOverflow: However I will personally go with ScheduledExecutorService instead of TimerTask.
 * <a href="https://stackoverflow.com/questions/22378422/how-to-use-timertask-with-lambdas">...</a>
 */
public class MyTimer {

    private Timer timer = new Timer();

    /**
     * Планирует выполнение указанной задачи с определенной периодичностью, начиная с указанного момента времени.
     * Заменяет метод Timer#schedule(TimerTask task, Date firstTime, long period).
     *
     * @param myTimerTask задача для выполнения
     * @param firstExecTime время первого выполнения
     * @param period период повторений выполнения задачи
     * @param periodUnit единицы измерения периода повторений выполнения задачи
     */
    public void schedule(MyTimerTask myTimerTask, ZonedDateTime firstExecTime, long period, TimeUnit periodUnit) {
        Date date = Date.from(firstExecTime.toInstant());
        long periodMillis = periodUnit.toMillis(period); // переводим period и periodUnit в мс
        timer.schedule(convert(myTimerTask), date, periodMillis);
    }

    /**
     * Планирует выполнение указанной задачи по истечении указанной задержки.
     * Заменяет метод Timer#schedule(TimerTask task, long delay).
     *
     * @param myTimerTask задача для выполнения
     * @param delay задержка по времени
     * @param delayUnit единицы измерения задержки
     */
    public void schedule(MyTimerTask myTimerTask, long delay, TimeUnit delayUnit) {
        long delayMillis = delayUnit.toMillis(delay); // переводим delay и delayUnit в мс
        timer.schedule(convert(myTimerTask), delayMillis);
    }

    /**
     * Планирует немедленное выполнение указанной задачи с указанной периодичностью.
     *
     * @param myTimerTask задача для выполнения
     * @param period задержка по времени
     * @param periodUnit единицы измерения задержки
     */
    public void schedulePeriodicExecution(MyTimerTask myTimerTask, long period, TimeUnit periodUnit) {
        var now = ZonedDateTime.now();
        schedule(myTimerTask, now, period, periodUnit);
    }

    // конвертирует MyTimerTask в TimerTask
    private TimerTask convert(MyTimerTask taskToDo) {
        return new TimerTask() {
            @Override
            public void run() {
                taskToDo.run();
            }
        };
    }

    /**
     * A task that can be scheduled by MyTimer.
     *
     * Моя замена классу java.util.TimerTask для использования вместе с MyTimer.
     * См. метод MyTimer#convert(MyTimerTask timerTask)
     *
     * Timer и TimerTask - это служебные классы Java, используемые для планирования задач в фоновом потоке.
     * В двух словах: TimerTask is the task to perform and Timer is the scheduler.
     */
    @FunctionalInterface
    public interface MyTimerTask {

        // task to do
        void run();

    }
}