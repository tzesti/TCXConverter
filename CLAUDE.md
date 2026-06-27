# TCXConverter — Claude context

## What this is
JavaFX 21 desktop app that converts Garmin Connect `.tcx` files to CSV-style plain text for pasting into AI chats. Drag-and-drop input, text area output, Copy and Save buttons.

## Build
- Gradle 9.6.1 with `org.openjfx.javafxplugin 0.1.0`
- Java 21 toolchain (set in `build.gradle`)
- Run: Gradle task `application > run`, or IntelliJ run config pointing at `com.tcxconverter.Main`
- The project is fully modular (`module-info.java`). `module-info.java` must `opens com.tcxconverter to javafx.fxml, javafx.graphics` — `javafx.graphics` is required because `LauncherImpl` reflectively instantiates `TcxConverterApp` at startup.

## Package layout
```
com.tcxconverter
├── Main.java                  entry point (delegates to TcxConverterApp)
├── TcxConverterApp.java       JavaFX Application subclass, loads main-view.fxml
├── controller/
│   └── MainController.java    drag-drop, Copy, Save handlers
├── formatter/
│   └── CsvFormatter.java      converts TcxData model → CSV string
├── model/
│   ├── TcxData.java           root: list of Activity + author name
│   ├── Activity.java          sport, id (start timestamp), laps, creator name
│   ├── Lap.java               lap summary fields + list of Trackpoint
│   └── Trackpoint.java        per-second GPS/sensor row
└── parser/
    └── TcxParser.java         DOM XML parser; namespace-aware (*), direct-child checks
```

## Output format
Three CSV sections in one text block:

```
# ACTIVITY
sport,id,creator,author,avg_latitude,avg_longitude

# LAPS
lap_number,start_time,total_time_seconds,distance_meters,max_speed_ms,
calories,avg_heart_rate_bpm,max_heart_rate_bpm,avg_cadence,intensity,trigger_method

# TRACKPOINTS
lap_number,elapsed_seconds,altitude_m,distance_m,
heart_rate_bpm,cadence,speed_ms,run_cadence,watts,sensor_state
```

- `avg_latitude` / `avg_longitude`: mean of all trackpoint coordinates, 4 decimal places (~11 m).
- `elapsed_seconds`: seconds since `Activity.id` timestamp, 2 decimal places.
- `altitude_m`, `distance_m`, `speed_ms`: 3 decimal places.
- Garmin extensions parsed from `TPX` element: `Speed`, `RunCadence`, `Watts`.

## Parser notes
- Uses DOM with `setNamespaceAware(true)` and `getElementsByTagNameNS("*", localName)` to handle varying namespace declarations across Garmin exports.
- `directChildText(parent, name)` checks `node.getParentNode() == parent` to avoid pulling nested elements (e.g., trackpoint `Cadence` when reading lap `Cadence`).
- Heart rate elements (`AverageHeartRateBpm`, `MaximumHeartRateBpm`, `HeartRateBpm`) are wrapped — parsed via `heartRateValue()` which finds the element then reads its `Value` child.

## Design decisions
- No data is discarded from the TCX file; all standard fields plus common Garmin extensions are emitted.
- `lat`/`lon` moved to `# ACTIVITY` as an average to reduce token count in trackpoint rows while preserving location context.
- Timestamps in trackpoints replaced with elapsed seconds for AI readability.
- `Main.java` exists as a non-JavaFX launcher class to avoid module bootstrap issues with some JVM configurations.
