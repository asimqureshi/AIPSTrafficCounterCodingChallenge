package qureshi.asim.aips;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TrafficAnalysisServiceTest {
    
    private TrafficAnalysisService service;
    
    @Before
    public void setUp() {
        service = new TrafficAnalysisService();
    }

    @Test
    public void givenValidLine_whenParseRecord_thenReturnsTrafficRecord() {
        // Given
        String line = "2021-12-01T05:00:00 5";

        // When
        TrafficRecord record = service.parseRecord(line);

        // Then
        assertEquals(LocalDateTime.of(2021, 12, 1, 5, 0, 0), record.getTimestamp());
        assertEquals(5, record.getCarCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInvalidFormat_whenParseRecord_thenThrows() {
        // Given
        String line = "invalid format";

        // When
        service.parseRecord(line);

        // Then -> exception
    }

    @Test
    public void givenTestResource_whenReadRecordsFromFile_thenParsesAllNonEmptyLines() throws Exception {
        // Given
        URL resourceUrl = getClass().getClassLoader().getResource("traffic_test_data.txt");
        assertNotNull("Test resource traffic_test_data.txt not found", resourceUrl);
        String path = Paths.get(resourceUrl.toURI()).toString();

        // When
        List<TrafficRecord> records = service.readRecordsFromFile(path);

        // Then
        assertEquals(3, records.size());
        assertEquals(LocalDateTime.of(2021, 12, 1, 5, 0, 0), records.get(0).getTimestamp());
        assertEquals(5, records.get(0).getCarCount());
    }

    @Test
    public void givenNoRecords_whenCalculateTotalCars_thenReturnsZero() {
        // Given
        List<TrafficRecord> records = java.util.Collections.emptyList();

        // When
        int total = service.calculateTotalCars(records);

        // Then
        assertEquals(0, total);
    }

    @Test
    public void givenRecords_whenCalculateTotalCars_thenReturnsTheSum() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
                new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 5),
                new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 0, 0), 7),
                new TrafficRecord(LocalDateTime.of(2021, 12, 2, 7, 0, 0), 4)
        );

        // When
        int total = service.calculateTotalCars(records);

        // Then
        assertEquals(16, total);
    }

    @Test
    public void givenMultipleDays_whenCalculateTotalCarsDaily_thenAggregatesPerDay() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 5),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 0, 0), 7),
            new TrafficRecord(LocalDateTime.of(2021, 12, 2, 7, 0, 0), 4)
        );

        // When
        Map<java.time.LocalDate, Integer> dailyTotals = service.calculateTotalCarsDaily(records);

        // Then
        assertEquals(2, dailyTotals.size());
        assertEquals(Integer.valueOf(12), dailyTotals.get(java.time.LocalDate.of(2021, 12, 1)));
        assertEquals(Integer.valueOf(4), dailyTotals.get(java.time.LocalDate.of(2021, 12, 2)));
    }

    @Test
    public void givenRecords_whenFindTopNRecordsWithMostCars_thenReturnsTopKDescending() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 5),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 30, 0), 25),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 0, 0), 15),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 30, 0), 30)
        );

        // When
        List<TrafficRecord> top3 = service.findTopKRecordsWithMostCars(records, 3);

        // Then
        assertEquals(3, top3.size());
        assertEquals(30, top3.get(0).getCarCount());
        assertEquals(25, top3.get(1).getCarCount());
        assertEquals(15, top3.get(2).getCarCount());
    }

    @Test
    public void givenNZero_whenFindTopKRecordsWithMostCars_thenReturnsEmptyList() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 5),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 30, 0), 25)
        );

        // When
        List<TrafficRecord> top0 = service.findTopKRecordsWithMostCars(records, 0);

        // Then
        assertTrue(top0.isEmpty());
    }

    @Test
    public void givenContiguousRecords_whenFindLeastCarsInPeriod_thenFindsMinSumWindowOf3() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 10),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 30, 0), 5),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 0, 0), 15),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 30, 0), 20),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 7, 0, 0), 8)
        );

        // When
        List<TrafficRecord> least = service.findContiguousRecordsWithLeastCars(records, 3);

        // Then
        assertEquals(3, least.size());
        assertEquals(30, least.stream().mapToInt(TrafficRecord::getCarCount).sum());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenTooFewRecords_whenFindLeastCarsInPeriod_thenThrows() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 5),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 30, 0), 10)
        );

        // When
        service.findContiguousRecordsWithLeastCars(records, 3);

        // Then -> exception
    }

    @Test(expected = IllegalStateException.class)
    public void givenNoContiguousRecords_whenFindLeastCarsInPeriod_thenThrows() {
        // Given (no consecutive 30-min spacing across any 3)
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 1),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 45, 0), 2),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 30, 0), 3),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 7, 15, 0), 4)
        );

        // When
        service.findContiguousRecordsWithLeastCars(records, 3);

        // Then -> exception
    }

    @Test
    public void givenWindowSize4_whenFindLeastCarsInPeriod_thenFindsMinSumWindow() {
        // Given (contiguous 30-min steps)
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 3),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 30, 0), 4),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 0, 0), 5),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 30, 0), 6),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 7, 0, 0), 100)
        );

        // When
        List<TrafficRecord> least = service.findContiguousRecordsWithLeastCars(records, 4);

        // Then
        assertEquals(4, least.size());
        assertEquals(3 + 4 + 5 + 6, least.stream().mapToInt(TrafficRecord::getCarCount).sum());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenWindowSize4AndTooFewRecords_whenFindLeastCarsInPeriod_thenThrows() {
        // Given
        List<TrafficRecord> records = Arrays.asList(
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 0, 0), 3),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 5, 30, 0), 4),
            new TrafficRecord(LocalDateTime.of(2021, 12, 1, 6, 0, 0), 5)
        );

        // When
        service.findContiguousRecordsWithLeastCars(records, 4);

        // Then -> exception
    }

    @Test
    public void givenUnsortedMap_whenFormatDailyTotals_thenOutputsSortedDateStrings() {
        // Given
        Map<java.time.LocalDate, Integer> dailyTotals = Map.of(
            java.time.LocalDate.of(2021, 12, 2), 20,
            java.time.LocalDate.of(2021, 12, 1), 15
        );

        // When
        List<String> formatted = service.formatDailyTotals(dailyTotals);

        // Then
        assertEquals(2, formatted.size());
        assertTrue(formatted.get(0).startsWith("2021-12-01"));
        assertTrue(formatted.get(1).startsWith("2021-12-02"));
    }
} 