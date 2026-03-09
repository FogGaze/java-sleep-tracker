package ru.yandex.practicum.sleeptracker.analyzer;

import ru.yandex.practicum.sleeptracker.SleepingSession;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class AverageDurationAnalyzer implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        double average = sessions.stream()
                .mapToDouble(session -> Duration.between(session.getStartSleepSession(),
                        session.getFinishSleepSession()).toMinutes())
                .average()
                .orElse(0.0);
        return new SleepAnalysisResult("Средняя продолжительность сессии сна в минутах", Math.round(average));
    }
}
