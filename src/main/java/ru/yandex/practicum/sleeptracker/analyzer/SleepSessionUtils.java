package ru.yandex.practicum.sleeptracker.analyzer;

import ru.yandex.practicum.sleeptracker.Chronotype;
import ru.yandex.practicum.sleeptracker.SleepingSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SleepSessionUtils {

    public static boolean overlapsNight(SleepingSession session, LocalDate nightDate) {
        LocalDateTime nightStart = nightDate.atTime(0, 0);
        LocalDateTime nightEnd = nightDate.atTime(6, 0);
        LocalDateTime sleepStart = session.getStartSleepSession();
        LocalDateTime sleepEnd = session.getFinishSleepSession();
        return sleepStart.isBefore(nightEnd) && sleepEnd.isAfter(nightStart);
    }

    public static boolean isNightSession(SleepingSession session) {
        LocalDate startDate = session.getStartSleepSession().toLocalDate();
        LocalDate endDate = session.getFinishSleepSession().toLocalDate();
        return startDate.datesUntil(endDate.plusDays(1))
                .anyMatch(date -> overlapsNight(session, date));
    }

    public static Chronotype getMostFrequentChronotype(Map<Chronotype, Long> counts) {
        long maxCount = counts.values().stream()
                .max(Long::compare)
                .orElse(0L);

        List<Chronotype> topTypes = counts.entrySet().stream()
                .filter(e -> e.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .toList();

        if (topTypes.size() > 1) {
            return Chronotype.PIGEON;
        } else {
            return topTypes.getFirst();
        }
    }
}
