package ru.yandex.practicum.sleeptracker.analyzer;

import ru.yandex.practicum.sleeptracker.SleepingSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static ru.yandex.practicum.sleeptracker.analyzer.SleepSessionUtils.overlapsNight;

public class SleeplessNightsAnalyzer implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        if (sessions.isEmpty()) {
            return new SleepAnalysisResult("Количество бессонных ночей", 0L);
        }

        SleepingSession first = sessions.stream()
                .min(Comparator.comparing(SleepingSession::getStartSleepSession))
                .orElseThrow();
        LocalDateTime firstStart = first.getStartSleepSession();
        LocalDate firstNightDate;
        if (firstStart.getHour() < 12) {
            firstNightDate = firstStart.toLocalDate();
        } else {
            firstNightDate = firstStart.toLocalDate().plusDays(1);
        }

        SleepingSession last = sessions.stream()
                .max(Comparator.comparing(SleepingSession::getFinishSleepSession))
                .orElseThrow();
        LocalDate lastNightDate = last.getFinishSleepSession().toLocalDate();

        long sleepLessNights = firstNightDate.datesUntil(lastNightDate.plusDays(1))
                .filter(date -> sessions.stream()
                        .noneMatch(session -> overlapsNight(session, date)))
                .count();
        return new SleepAnalysisResult("Количество бессонных ночей", sleepLessNights);
    }
}
