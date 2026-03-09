package ru.yandex.practicum.sleeptracker;

import ru.yandex.practicum.sleeptracker.analyzer.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SleepTrackerApp {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    private static final List<Function<List<SleepingSession>, SleepAnalysisResult>> ANALYZERS = List.of(
            new SessionsCountAnalyzer(),
            new MinDurationAnalyzer(),
            new MaxDurationAnalyzer(),
            new AverageDurationAnalyzer(),
            new BadSessionsAnalyzer(),
            new SleeplessNightsAnalyzer(),
            new ChronotypeAnalyzer()
    );

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Укажите путь к файлу с логом сна в аргументах запуска.");
            return;
        }
        String filePath = args[0];
        List<SleepingSession> sessions = readLog(filePath);

        ANALYZERS.stream()
                .map(analyzer -> analyzer.apply(sessions))
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
}