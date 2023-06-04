package com.mycompany.my;

/**
 * A task that can be scheduled by MyTimer.
 * Является моей реализацией-заменой для java.util.TimerTask.
 *
 * Timer и TimerTask - это служебные классы Java, используемые для планирования задач в фоновом потоке.
 * В двух словах: TimerTask is the task to perform and Timer is the scheduler.
 * */

@FunctionalInterface
public interface MyTimerTask {

    // task to do
    void run();

}