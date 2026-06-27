# TCX Converter

Converts Garmin Connect `.tcx` activity files into CSV-style plain text suitable for pasting into AI chats.

## Requirements

- Java 21+
- IntelliJ IDEA (recommended) or any IDE with Gradle support

## Opening in IntelliJ

1. **File → Open** → select the `TCXConverter` directory
2. IntelliJ detects `build.gradle` and prompts to import — click **Trust Project**
3. Wait for Gradle sync to download JavaFX 21
4. Run via **Gradle tool window → Tasks → application → run**,  
   or create a Run Configuration: Main class `com.tcxconverter.Main`, module `com.tcxconverter`

## Usage

1. Launch the app
2. Drag and drop a `.tcx` file exported from Garmin Connect onto the window
3. The converted text appears immediately in the text area
4. **Copy All** — copies the full text to the clipboard
5. **Save as .txt** — opens a save dialog pre-filled with the source file's location and name

## Output format

```
# ACTIVITY
sport,id,creator,author,avg_latitude,avg_longitude
Running,2024-05-01T08:00:00.000Z,Forerunner 965,Garmin Connect,37.3382,126.8323

# LAPS
lap_number,start_time,total_time_seconds,distance_meters,max_speed_ms,calories,avg_heart_rate_bpm,max_heart_rate_bpm,avg_cadence,intensity,trigger_method
1,2024-05-01T08:00:00.000Z,600.0,1500.0,3.521,120,145,165,85,Active,Manual
...

# TRACKPOINTS
lap_number,elapsed_seconds,altitude_m,distance_m,heart_rate_bpm,cadence,speed_ms,run_cadence,watts,sensor_state
1,0.00,52.400,0.000,130,84,3.124,168,,Present
1,1.00,52.398,3.121,131,84,3.130,168,,Present
...
```

| Column | Notes |
|---|---|
| `avg_latitude` / `avg_longitude` | Mean of all GPS points, 4 decimal places |
| `elapsed_seconds` | Seconds since activity start, 2 decimal places |
| `altitude_m`, `distance_m`, `speed_ms` | 3 decimal places |
| `run_cadence` | Steps/min (single foot), from Garmin extension |
| `watts` | Power in watts, cycling activities |

Empty cells mean the field was absent in the source file.
