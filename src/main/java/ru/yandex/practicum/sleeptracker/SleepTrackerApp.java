package ru.yandex.practicum.sleeptracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SleepTrackerApp {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    private static final List<Function<List<SleepingSession>, SleepAnalysisResult>> FUNCTIONS = List.of(
            findSessionsSize(),
            findMinSession(),
            findMaxSession(),
            findAverageSession(),
            findBadNights(),
            findLessSleepNights(),
            findChronotype()
    );

    public static void main(String[] args) {
        String filePath = args[0];
        List<SleepingSession> sessions = readLog(filePath);

        FUNCTIONS.stream()
                .map(function -> function.apply(sessions))
                .forEach(result ->
                        System.out.println(result.getDescription() + ": " + result.getValue())
                );
    }

    public static List<SleepingSession> readLog(String path) {
        try (Stream<String> lines = Files.lines(Path.of(path))) {
            return lines
                    .filter(line -> !line.isBlank())
                    .map(SleepTrackerApp::parseSession)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Optional<SleepingSession> parseSession(String line) {
        String[] parts = line.split(";");
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            LocalDateTime start = LocalDateTime.parse(parts[0], formatter);
            LocalDateTime finish = LocalDateTime.parse(parts[1], formatter);
            SleepQuality quality = SleepQuality.valueOf(parts[2]);
            return Optional.of(new SleepingSession(start, finish, quality));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

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

    public static Function<List<SleepingSession>, SleepAnalysisResult> findSessionsSize() {
        return sessions -> new SleepAnalysisResult("Общее количество сессий сна", sessions.size());
    }

    public static Function<List<SleepingSession>, SleepAnalysisResult> findMinSession() {
        return sessions -> {
            long min = sessions.stream()
                    .map(session -> Duration.between(session.getStartSleepSession(), session.getFinishSleepSession()).toMinutes())
                    .min(Comparator.naturalOrder())
                    .orElse(0L);
            return new SleepAnalysisResult("Минимальная продолжительность сессии сна в минутах", min);
        };
    }

    public static Function<List<SleepingSession>, SleepAnalysisResult> findMaxSession() {
        return sessions -> {
            long max = sessions.stream()
                    .map(session -> Duration.between(session.getStartSleepSession(), session.getFinishSleepSession()).toMinutes())
                    .max(Comparator.naturalOrder())
                    .orElse(0L);
            return new SleepAnalysisResult("Максимальная продолжительность сессии сна в минутах", max);
        };
    }

    public static Function<List<SleepingSession>, SleepAnalysisResult> findAverageSession() {
        return sessions -> {
            double average = sessions.stream()
                    .mapToDouble(session -> Duration.between(session.getStartSleepSession(), session.getFinishSleepSession()).toMinutes())
                    .average()
                    .orElse(0.0);
            return new SleepAnalysisResult("Средняя продолжительность сессии сна в минутах", Math.round(average));
        };
    }

    public static Function<List<SleepingSession>, SleepAnalysisResult> findBadNights() {
        return sessions -> new SleepAnalysisResult("Количество сессий с плохим качеством сна", sessions.stream()
                .filter(s -> s.getSleepQuality() == SleepQuality.BAD).count());
    }

    public static Function<List<SleepingSession>, SleepAnalysisResult> findLessSleepNights() {
        return sessions -> {
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
        };
    }

    public static Function<List<SleepingSession>, SleepAnalysisResult> findChronotype() {
        return sessions -> {
            if (sessions.isEmpty()) {
                return new SleepAnalysisResult("Хронотип пользователя", "не определен");
            }

            Map<Chronotype, Long> counts = sessions.stream()
                    .filter(SleepTrackerApp::isNightSession)
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
        };
    }
}