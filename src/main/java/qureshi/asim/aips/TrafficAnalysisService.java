package qureshi.asim.aips;

import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/**
 * Service class for analyzing traffic counter data.
 */
public class TrafficAnalysisService {
    
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int INTERVAL_MINS = 30;
    
    /**
     * Reads a file of traffic data and returns parsed records.
     */
    public List<TrafficRecord> readRecordsFromFile(@NonNull final String inputFile) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(inputFile));
        return lines.stream()
                .filter(line -> !line.trim().isEmpty())
                .map(this::parseRecord)
                .toList();
    }

    /**
     * Parses a line from the input file into a TrafficRecord.
     */
    public TrafficRecord parseRecord(@NonNull final String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid line format: " + line);
        }
        
        try {
            LocalDateTime timestamp = LocalDateTime.parse(parts[0], INPUT_FORMATTER);
            int carCount = Integer.parseInt(parts[1]);
            
            return new TrafficRecord(timestamp, carCount);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + parts[0], e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid car count format: " + parts[1], e);
        }
    }
    
    /**
     * Calculates the total number of cars seen.
     */
    public int calculateTotalCars(final List<TrafficRecord> records) {

        if(records == null) {
            return 0;
        }

        return records.stream()
                .mapToInt(TrafficRecord::getCarCount)
                .sum();
    }
    
    /**
     * Groups records by date and calculates daily totals.
     */
    public Map<LocalDate, Integer> calculateTotalCarsDaily(@NonNull final List<TrafficRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getTimestamp().toLocalDate(),
                    Collectors.summingInt(TrafficRecord::getCarCount)
                ));
    }
    
    /**
     * Finds the top n half-hour periods with the most cars.
     */
    public List<TrafficRecord> findTopNRecordsWithMostCars(@NonNull final List<TrafficRecord> records, int n) {
        return records.stream()
                .sorted((r1, r2) -> Integer.compare(r2.getCarCount(), r1.getCarCount()))
                .limit(n)
                .toList();
    }


    /**
     * Finds the contiguous window of given size (half-hour records) with the least cars.
     */
    public List<TrafficRecord> findNContiguousRecordsWithLeastCars(@NonNull final List<TrafficRecord> records,
                                                                   int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be > 0");
        }
        if (records.size() < windowSize) {
            throw new IllegalArgumentException("Need at least " + windowSize + " records to find a contiguous window");
        }

        List<TrafficRecord> leastCarsPeriod = null;
        int minTotalCars = Integer.MAX_VALUE;

        for (int i = 0; i <= records.size() - windowSize; i++) {
            List<TrafficRecord> period = records.subList(i, i + windowSize);
            if (isContiguous(period)) {
                int totalCars = period.stream().mapToInt(TrafficRecord::getCarCount).sum();
                if (totalCars < minTotalCars) {
                    minTotalCars = totalCars;
                    leastCarsPeriod = new ArrayList<>(period);
                }
            }
        }

        if (leastCarsPeriod == null) {
            throw new IllegalStateException("No contiguous window of size " + windowSize + " found");
        }

        return leastCarsPeriod;
    }

    
    private boolean isContiguous(final List<TrafficRecord> period) {
        if (period == null || period.isEmpty()) return false;
        for (int i = 0; i < period.size() - 1; i++) {
            LocalDateTime current = period.get(i).getTimestamp();
            LocalDateTime next = period.get(i + 1).getTimestamp();
            if (!next.equals(current.plusMinutes(INTERVAL_MINS))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Formats daily totals for output.
     */
    public List<String> formatDailyTotals(@NonNull final Map<LocalDate, Integer> dailyTotals) {
        return dailyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s %d", 
                    entry.getKey().format(OUTPUT_FORMATTER), 
                    entry.getValue()))
                .toList();
    }
} 