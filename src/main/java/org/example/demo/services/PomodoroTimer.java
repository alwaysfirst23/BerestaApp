package org.example.demo.services;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Класс PomodoroTimer реализует логику таймера Pomodoro
 * с автоматическим обновлением UI через JavaFX Properties.
 */
public class PomodoroTimer {
    private Timeline timeline;
    @Getter
    private int workDuration;    // в минутах
    @Getter
    private int breakDuration;   // в минутах
    private int secondsRemaining;
    private boolean isWorkTime;

    // Observable свойства для автоматического обновления UI
    private final StringProperty remainingTime = new SimpleStringProperty();
    private final StringProperty mode = new SimpleStringProperty();
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    @Setter
    private Consumer<Boolean> onModeChanged;

    private void switchMode() {
        isWorkTime = !isWorkTime;
        secondsRemaining = (isWorkTime ? workDuration : breakDuration) * 60;

        if (onModeChanged != null) {
            onModeChanged.accept(isWorkTime);
        }
    }

    /**
     * Конструктор класса PomodoroTimer.
     *
     * @param workDuration продолжительность рабочего времени в минутах
     * @param breakDuration продолжительность перерыва в минутах
     */
    public PomodoroTimer(int workDuration, int breakDuration) {
        this.workDuration = workDuration;
        this.breakDuration = breakDuration;
        this.isWorkTime = true;
        this.secondsRemaining = workDuration * 60;

        // Инициализируем начальные значения
        updateDisplay();
    }

    /**
     * Запускает таймер
     */
    public void start() {
        if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        isRunning.set(true);
    }

    /**
     * Приостанавливает таймер
     */
    public void pause() {
        if (timeline != null) {
            timeline.pause();
            isRunning.set(false);
        }
    }

    /**
     * Сбрасывает таймер в начальное состояние
     */
    public void reset() {
        if (timeline != null) {
            timeline.stop();
            isRunning.set(false);
        }
        isWorkTime = true;
        secondsRemaining = workDuration * 60;
        updateDisplay();
    }

    /**
     * Обновляет таймер каждую секунду
     */
    private void updateTimer() {
        secondsRemaining--;
        updateDisplay();

        if (secondsRemaining <= 0) {
            switchMode();
        }
    }

    /**
     * Обновляет отображаемые значения
     */
    private void updateDisplay() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        remainingTime.set(String.format("%02d:%02d", minutes, seconds));
        mode.set(isWorkTime ? "Режим: Работа" : "Режим: Отдых");
    }

    // ==================== Свойства для привязки ====================

    public StringProperty remainingTimeProperty() {
        return remainingTime;
    }

    public StringProperty modeProperty() {
        return mode;
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public boolean isWorkTime() {
        return isWorkTime;
    }

    /**
     * Возвращает оставшееся время в секундах (для тестов)
     */
    protected int getSecondsRemaining() {
        return secondsRemaining;
    }

    public void setDurations(int workDuration, int breakDuration) {
        this.workDuration = workDuration;
        this.breakDuration = breakDuration;
        reset();
    }
}
