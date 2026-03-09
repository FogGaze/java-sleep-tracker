package ru.yandex.practicum.sleeptracker.analyzer;

import ru.yandex.practicum.sleeptracker.Chronotype;
import ru.yandex.practicum.sleeptracker.SleepingSession;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


import static ru.yandex.practicum.sleeptracker.analyzer.SleepSessionUtils.*;


public class ChronotypeAnalyzer implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        if (sessions.isEmpty()) {
            return new SleepAnalysisResult("Хронотип пользователя", "не определен");
        }

        Map<Chronotype, Long> counts = sessions.stream()
                .filter(SleepSessionUtils::isNightSession)
                .map(session -> {
                    LocalDateTime start = session.getStartSleepSession();
                    LocalDateTime end = session.getFinishSleepSession();
                    LocalTime startTime = start.toLocalTime();
                    LocalTime endTime = end.toLocalTime();

                    if (startTime.isAfter(LocalTime.of(23, 0)) && endTime.isAfter(LocalTime.of(9, 0))) {
                        return Chronotype.OWL;
                    } else if (startTime.isBefore(LocalTime.of(22, 0)) && endTime.isBefore(LocalTime.of(7, 0))) {
                        return Chronotype.LARK;
                    } else {
                        return Chronotype.PIGEON;
                    }
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Chronotype result = getMostFrequentChronotype(counts);
        return new SleepAnalysisResult("Хронотип пользователя", result.toString());
    }
}
