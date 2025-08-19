# Traffic Counter Analysis Engine

An automated traffic counter sits by a road and counts the number of cars that go past. Every half-hour the counter outputs the number of cars seen and resets the counter to zero.

This Java application reads a file containing traffic data and provides comprehensive analysis including:
- Total car count
- Daily car counts
- Top 3 half-hour periods with most cars
- 1.5 hour period with least cars (3 contiguous half-hour records)

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

To build the project, run:

```bash
mvn clean compile
```

To run tests:

```bash
mvn test
```

To create an executable JAR:

```bash
mvn clean package
```

## Usage

### Running the Application

After building, you can run the application in the following way:

```bash
mvn -q -DskipTests package && java -Djava.util.logging.config.file=src/main/resources/logging.properties -jar target/traffic-counter-analysis-1.0.0.jar src/main/resources/traffic_data.txt
```

### Input File Format

The input file should contain one traffic record per line in the following format:
```
yyyy-MM-ddTHH:mm:ss car_count
```

Example:
```
2021-12-01T05:00:00 5
2021-12-01T05:30:00 12
2021-12-01T06:00:00 14
```
