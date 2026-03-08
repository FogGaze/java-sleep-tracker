package ru.yandex.practicum.sleeptracker;

import java.time.LocalDateTime;

public class SleepingSession {
    private final LocalDateTime startSleepSession;
    private final LocalDateTime finishSleepSession;
    private final SleepQuality sleepQuality;

    public SleepingSession(LocalDateTime start, LocalDateTime finish, SleepQuality sleepQuality) {
        this.startSleepSession = start;
        this.finishSleepSession = finish;
        this.sleepQuality = sleepQuality;
    }

    public LocalDateTime getStartSleepSession() {
        return startSleepSession;
    }

    public LocalDateTime getFinishSleepSession() {
        return finishSleepSession;
    }

    public SleepQuality getSleepQuality() {
        return sleepQuality;
    }
}
