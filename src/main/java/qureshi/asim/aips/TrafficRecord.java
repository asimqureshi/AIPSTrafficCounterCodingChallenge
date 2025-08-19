package qureshi.asim.aips;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a traffic record with timestamp and car count.
 */
public record TrafficRecord(LocalDateTime timestamp, int carCount) {

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getCarCount() {
        return carCount;
    }

    @Override
    public String toString() {
        return String.format("%s %d",
            timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
            carCount);
    }
}