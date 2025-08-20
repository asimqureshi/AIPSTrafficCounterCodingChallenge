package qureshi.asim.aips;

import java.io.IOException;
import java.util.List;

/**
 * Main class for the Traffic Counter Analysis Engine.
 * Reads traffic data from a file and provides various analytics.
 */
public class TrafficCounterAnalysis {
    private static final System.Logger LOGGER = System.getLogger(TrafficCounterAnalysis.class.getName());

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.log(System.Logger.Level.INFO, "Usage: java TrafficCounterAnalysis <input-file>");
            LOGGER.log(System.Logger.Level.INFO, "Example: java TrafficCounterAnalysis traffic_data.txt");
            System.exit(1);
        }

        String inputFile = args[0];

        try {
            TrafficCounterAnalysis analyzer = new TrafficCounterAnalysis();
            analyzer.analyzeTrafficData(inputFile);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Error analyzing traffic data: {0}", e.getMessage());
            LOGGER.log(System.Logger.Level.ERROR, "Exception", e);
            System.exit(1);
        }
    }

    /**
     * Main method to analyze traffic data from a file.
     */
    public void analyzeTrafficData(String inputFile) throws IOException {
        TrafficAnalysisService service = new TrafficAnalysisService();

        // Read and parse the input file via service
        List<TrafficRecord> records = service.readRecordsFromFile(inputFile);

        if (records.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "No valid traffic records found in the input file.");
            return;
        }

        LOGGER.log(System.Logger.Level.INFO, "=== Traffic Counter Analysis Results ===");

        // 1. Total cars seen
        int totalCars = service.calculateTotalCars(records);
        LOGGER.log(System.Logger.Level.INFO, "Total cars seen: {0}", totalCars);
        // Intentionally no blank line to avoid multi-row output

        // 2. Daily totals
        LOGGER.log(System.Logger.Level.INFO, "Daily car counts:");
        var dailyTotals = service.calculateTotalCarsDaily(records);
        List<String> formattedDailyTotals = service.formatDailyTotals(dailyTotals);
        formattedDailyTotals.forEach(line -> LOGGER.log(System.Logger.Level.INFO, line));

        // 3. Top 3 half-hour periods
        LOGGER.log(System.Logger.Level.INFO, "Top 3 half-hour periods with most cars:");
        List<TrafficRecord> top3HalfHours = service.findTopKRecordsWithMostCars(records, 3);
        top3HalfHours.forEach(record -> LOGGER.log(System.Logger.Level.INFO, record.toString()));

        // 4. 1.5 hour period with least cars
        LOGGER.log(System.Logger.Level.INFO, "1.5 hour period with least cars:");
        try {
            List<TrafficRecord> leastCarsPeriod = service.findContiguousRecordsWithLeastCars(records, 3);
            int periodTotal = leastCarsPeriod.stream()
                    .mapToInt(TrafficRecord::getCarCount)
                    .sum();

            LOGGER.log(System.Logger.Level.INFO, "Period total: {0} cars", periodTotal);
            LOGGER.log(System.Logger.Level.INFO, "Records in this period:");
            leastCarsPeriod.forEach(record -> LOGGER.log(System.Logger.Level.INFO, "  {0}", record.toString()));
        } catch (IllegalStateException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Could not determine 1.5 hour period: {0}", e.getMessage());
        }
    }
} 