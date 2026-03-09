package ru.yandex.practicum.sleeptracker.analyzer;

import ru.yandex.practicum.sleeptracker.SleepingSession;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class MinDurationAnalyzer implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        long min = sessions.stream()
                .map(session -> Duration.between(session.getStartSleepSession(),
                        session.getFinishSleepSession()).toMinutes())
                .min(Comparator.naturalOrder())
                .orElse(0L);
        return new SleepAnalysisResult("Минимальная продолжительность сессии сна в минутах", min);
    }
}
