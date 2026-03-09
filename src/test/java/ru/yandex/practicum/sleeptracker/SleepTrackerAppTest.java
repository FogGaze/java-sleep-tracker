package ru.yandex.practicum.sleeptracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.sleeptracker.analyzer.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SleepTrackerAppTest {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
    private final SessionsCountAnalyzer sessionCA = new SessionsCountAnalyzer();
    private final MinDurationAnalyzer minDA = new MinDurationAnalyzer();
    private final MaxDurationAnalyzer maxDA = new MaxDurationAnalyzer();
    private final AverageDurationAnalyzer averageDA = new AverageDurationAnalyzer();
    private final BadSessionsAnalyzer badSA = new BadSessionsAnalyzer();
    private final SleeplessNightsAnalyzer sleepLNA = new SleeplessNightsAnalyzer();
    private final ChronotypeAnalyzer chronotypeA = new ChronotypeAnalyzer();

    @Test
    @DisplayName("Проверка чтения строки")
    void testParseSession() {
        String line = "12.12.12 12:12;12.12.12 15:15;BAD";
        Optional<SleepingSession> result = SleepTrackerApp.parseSession(line);
        assertTrue(result.isPresent());
        SleepingSession session = result.get();
        assertEquals(LocalDateTime.parse("12.12.12 12:12", formatter), session.getStartSleepSession());
        assertEquals(LocalDateTime.parse("12.12.12 15:15", formatter), session.getFinishSleepSession());
        assertEquals(SleepQuality.BAD, session.getSleepQuality());
    }

    @Test
    @DisplayName("Проверка чтения некорректной строки")
    void testParseSessionNotCorrectLine() {
        assertAll(
                () -> assertFalse(SleepTrackerApp.parseSession("12.12.12 12:12 12.12.12 15:15 BAD").isPresent()),
                () -> assertFalse(SleepTrackerApp.parseSession("12.12.12 12:12;12.12.12 15:15;хорошо").isPresent()),
                () -> assertFalse(SleepTrackerApp.parseSession("12.12.12 12:12;12.12.12 15:15;").isPresent()),
                () -> assertFalse(SleepTrackerApp.parseSession("12.12.12 99:99;12.12.12 99:99;BAD").isPresent()),
                () -> assertFalse(SleepTrackerApp.parseSession("99.99.99 12:12;12.12.12 15:15;;BAD").isPresent()),
                () -> assertFalse(SleepTrackerApp.parseSession("12.12.12 12:12;1;BAD").isPresent())
        );
    }


    @Test
    @DisplayName("Проверка подсчёта количества сессий")
    void testSessionsSize() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 23:00", formatter).plusDays(i);
            LocalDateTime end = LocalDateTime.parse("02.02.26 06:45", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.GOOD));
        }

        SleepAnalysisResult result = sessionCA.apply(sessions);
        assertEquals(10, result.getValue());
        assertEquals("Общее количество сессий сна", result.getDescription());
    }

    @Test
    @DisplayName("Проверка подсчёта количества сессий с пустым списком")
    void testSessionsSizeWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = sessionCA.apply(sessions);
        assertEquals(0, result.getValue());
        assertEquals("Общее количество сессий сна", result.getDescription());
    }


    @Test
    @DisplayName("Проверка поиска минимальной продолжительности сессии")
    void testFindMinSession() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 00:00", formatter).plusDays(i).plusHours(i);
            LocalDateTime end = LocalDateTime.parse("01.02.26 12:00", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.NORMAL));
        }

        SleepAnalysisResult result = minDA.apply(sessions);
        assertEquals(180L, result.getValue());
        assertEquals("Минимальная продолжительность сессии сна в минутах", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска минимальной продолжительности сессии. Одна сессия")
    void testFindMinSessionWithOneSession() {
        List<SleepingSession> sessions = new ArrayList<>();
        sessions.add(new SleepingSession(LocalDateTime.parse("01.02.26 00:00", formatter),
                LocalDateTime.parse("01.02.26 10:00", formatter), SleepQuality.NORMAL));

        SleepAnalysisResult result = minDA.apply(sessions);
        assertEquals(600L, result.getValue());
        assertEquals("Минимальная продолжительность сессии сна в минутах", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска минимальной продолжительности сессии с пустым списком")
    void testFindMinSessionWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = minDA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals("Минимальная продолжительность сессии сна в минутах", result.getDescription());
    }


    @Test
    @DisplayName("Проверка поиска максимальной продолжительности сессии")
    void testFindMaxSession() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 00:00", formatter).plusDays(i).plusHours(i);
            LocalDateTime end = LocalDateTime.parse("01.02.26 12:00", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.NORMAL));
        }

        SleepAnalysisResult result = maxDA.apply(sessions);
        assertEquals(720L, result.getValue());
        assertEquals("Максимальная продолжительность сессии сна в минутах", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска максимальной продолжительности сессии. Одна сессия")
    void testFindMaxSessionWithOneSession() {
        List<SleepingSession> sessions = new ArrayList<>();
        sessions.add(new SleepingSession(LocalDateTime.parse("01.02.26 00:00", formatter),
                LocalDateTime.parse("01.02.26 10:00", formatter), SleepQuality.NORMAL));

        SleepAnalysisResult result = maxDA.apply(sessions);
        assertEquals(600L, result.getValue());
        assertEquals("Максимальная продолжительность сессии сна в минутах", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска максимальной продолжительности сессии с пустым списком")
    void testFindMaxSessionWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = maxDA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals("Максимальная продолжительность сессии сна в минутах", result.getDescription());
    }


    @Test
    @DisplayName("Проверка поиска средней продолжительности сессий")
    void testFindAverageSession() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 00:00", formatter).plusDays(i).plusHours(i);
            LocalDateTime end = LocalDateTime.parse("01.02.26 12:00", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.NORMAL));
        }

        SleepAnalysisResult result = averageDA.apply(sessions);
        assertEquals(450L, result.getValue());
        assertEquals("Средняя продолжительность сессии сна в минутах", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска средней продолжительности сессий. Одна сессия")
    void testFindAverageSessionWithOneSession() {
        List<SleepingSession> sessions = new ArrayList<>();
        sessions.add(new SleepingSession(LocalDateTime.parse("01.02.26 00:00", formatter),
                LocalDateTime.parse("01.02.26 10:00", formatter), SleepQuality.NORMAL));

        SleepAnalysisResult result = averageDA.apply(sessions);
        assertEquals(600L, result.getValue());
        assertEquals("Средняя продолжительность сессии сна в минутах", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска средней продолжительности сессий с пустым списком")
    void testFindAverageSessionWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = averageDA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals("Средняя продолжительность сессии сна в минутах", result.getDescription());
    }


    @Test
    @DisplayName("Проверка поиска количества сессий с плохим качеством сна")
    void testFindBadSessions() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 00:00", formatter).plusDays(i).plusHours(i);
            LocalDateTime end = LocalDateTime.parse("01.02.26 12:00", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.BAD));
        }
        sessions.add(new SleepingSession(LocalDateTime.parse("10.02.26 23:59", formatter),
                LocalDateTime.parse("11.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 22:00", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = badSA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(10L, result.getValue());
        assertEquals(12, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество сессий с плохим качеством сна", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска количества сессий с плохим качеством сна при отсутствии плохих сессий")
    void testFindBadSessionWithZeroBadSessions() {
        List<SleepingSession> sessions = new ArrayList<>();
        sessions.add(new SleepingSession(LocalDateTime.parse("10.02.26 23:59", formatter),
                LocalDateTime.parse("11.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 22:00", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = badSA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals(2, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество сессий с плохим качеством сна", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска количества сессий с плохим качеством сна при отсутствии сессий")
    void testFindBadSessionWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = badSA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals(0, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество сессий с плохим качеством сна", result.getDescription());
    }


    @Test
    @DisplayName("Проверка поиска бессонных ночей")
    void testFindLessSleepSessions() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 10:00", formatter).plusDays(i).minusHours(i);
            LocalDateTime end = LocalDateTime.parse("01.02.26 12:00", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.BAD));
        }
        sessions.add(new SleepingSession(LocalDateTime.parse("10.02.26 23:59", formatter),
                LocalDateTime.parse("11.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 22:00", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = sleepLNA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(5L, result.getValue());
        assertEquals(12, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество бессонных ночей", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска бессонных ночей при отсутствии таковых")
    void testFindZeroLessSleepSessions() {
        List<SleepingSession> sessions = new ArrayList<>();
        sessions.add(new SleepingSession(LocalDateTime.parse("10.02.26 23:59", formatter),
                LocalDateTime.parse("11.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 22:00", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = sleepLNA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals(2, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество бессонных ночей", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска бессонных ночей при отсутствии сессий")
    void testFindLessSleepSessionsWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = sleepLNA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals(0, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество бессонных ночей", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска бессонных ночей. Пограничные случаи")
    void testFindLessSleepSessionsWithBorder() {
        List<SleepingSession> sessions = new ArrayList<>();
        sessions.add(new SleepingSession(LocalDateTime.parse("01.02.26 15:00", formatter),
                LocalDateTime.parse("01.02.26 17:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("02.02.26 01:00", formatter),
                LocalDateTime.parse("02.02.26 08:00", formatter), SleepQuality.GOOD));
        sessions.add(new SleepingSession(LocalDateTime.parse("02.02.26 23:00", formatter),
                LocalDateTime.parse("03.02.26 07:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = sleepLNA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals(0L, result.getValue());
        assertEquals(3, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Количество бессонных ночей", result.getDescription());
    }


    @Test
    @DisplayName("Проверка поиска хронотипа голубь")
    void testFindChronotypePigeon() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 23:00", formatter).plusDays(i);
            LocalDateTime end = LocalDateTime.parse("02.02.26 09:00", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.BAD));
        }
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 23:59", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("12.02.26 22:00", formatter),
                LocalDateTime.parse("13.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = chronotypeA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals("PIGEON", result.getValue());
        assertEquals(12, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Хронотип пользователя", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска хронотипа сова")
    void testFindChronotypeOwl() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 23:01", formatter).plusDays(i);
            LocalDateTime end = LocalDateTime.parse("02.02.26 09:01", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.BAD));
        }
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 23:59", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("12.02.26 22:00", formatter),
                LocalDateTime.parse("13.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = chronotypeA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals("OWL", result.getValue());
        assertEquals(12, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Хронотип пользователя", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска хронотипа жаворонок")
    void testFindChronotypeLark() {
        List<SleepingSession> sessions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = LocalDateTime.parse("01.02.26 21:59", formatter).plusDays(i);
            LocalDateTime end = LocalDateTime.parse("02.02.26 06:59", formatter).plusDays(i);
            sessions.add(new SleepingSession(start, end, SleepQuality.BAD));
        }
        sessions.add(new SleepingSession(LocalDateTime.parse("11.02.26 23:59", formatter),
                LocalDateTime.parse("12.02.26 12:00", formatter), SleepQuality.NORMAL));
        sessions.add(new SleepingSession(LocalDateTime.parse("12.02.26 22:00", formatter),
                LocalDateTime.parse("13.02.26 12:00", formatter), SleepQuality.GOOD));

        SleepAnalysisResult result = chronotypeA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals("LARK", result.getValue());
        assertEquals(12, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Хронотип пользователя", result.getDescription());
    }

    @Test
    @DisplayName("Проверка поиска хронотипа при пустом списке")
    void testFindChronotypeWithZeroSessions() {
        List<SleepingSession> sessions = new ArrayList<>();

        SleepAnalysisResult result = chronotypeA.apply(sessions);
        SleepAnalysisResult resultSize = sessionCA.apply(sessions);
        assertEquals("не определен", result.getValue());
        assertEquals(0, resultSize.getValue());
        assertEquals("Общее количество сессий сна", resultSize.getDescription());
        assertEquals("Хронотип пользователя", result.getDescription());
    }
}