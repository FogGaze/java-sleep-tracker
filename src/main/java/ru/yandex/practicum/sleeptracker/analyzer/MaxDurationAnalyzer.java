package ru.yandex.practicum.sleeptracker.analyzer;

import ru.yandex.practicum.sleeptracker.SleepingSession;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class MaxDurationAnalyzer implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        long max = sessions.stream()
                .map(session -> Duration.between(session.getStartSleepSession(),
                        session.getFinishSleepSession()).toMinutes())
                .max(Comparator.naturalOrder())
                .orElse(0L);
        return new SleepAnalysisResult("Максимальная продолжительность сессии сна в минутах", max);
    }
}
