package com.tcxconverter.formatter;

import com.tcxconverter.model.Activity;
import com.tcxconverter.model.Lap;
import com.tcxconverter.model.TcxData;
import com.tcxconverter.model.Trackpoint;

import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;

public class CsvFormatter {

    public String format(TcxData data) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.activities().size(); i++) {
            if (i > 0) sb.append("\n");
            appendActivity(sb, data.activities().get(i), data.authorName());
        }

        return sb.toString().stripTrailing();
    }

    private void appendActivity(StringBuilder sb, Activity activity, String authorName) {
        List<Trackpoint> allPoints = activity.laps().stream()
                .flatMap(l -> l.trackpoints().stream())
                .toList();
        OptionalDouble avgLat = allPoints.stream()
                .filter(tp -> tp.latitude() != null)
                .mapToDouble(Trackpoint::latitude)
                .average();
        OptionalDouble avgLon = allPoints.stream()
                .filter(tp -> tp.longitude() != null)
                .mapToDouble(Trackpoint::longitude)
                .average();

        sb.append("# ACTIVITY\n");
        sb.append("sport,id,creator,author,avg_latitude,avg_longitude\n");
        sb.append(csv(activity.sport())).append(',')
          .append(csv(activity.id())).append(',')
          .append(csv(activity.creatorName())).append(',')
          .append(csv(authorName)).append(',')
          .append(avgLat.isPresent() ? String.format("%.4f", avgLat.getAsDouble()) : "").append(',')
          .append(avgLon.isPresent() ? String.format("%.4f", avgLon.getAsDouble()) : "").append('\n');

        sb.append("\n# LAPS\n");
        sb.append("lap_number,start_time,total_time_seconds,distance_meters,")
          .append("max_speed_ms,calories,avg_heart_rate_bpm,max_heart_rate_bpm,")
          .append("avg_cadence,intensity,trigger_method\n");
        for (Lap lap : activity.laps()) {
            appendLapSummary(sb, lap);
        }

        Instant activityStart = parseInstant(activity.id());

        sb.append("\n# TRACKPOINTS\n");
        sb.append("lap_number,elapsed_seconds,altitude_m,distance_m,")
          .append("heart_rate_bpm,cadence,speed_ms,run_cadence,watts,sensor_state\n");
        for (Lap lap : activity.laps()) {
            for (Trackpoint tp : lap.trackpoints()) {
                appendTrackpoint(sb, tp, activityStart);
            }
        }
    }

    private void appendLapSummary(StringBuilder sb, Lap lap) {
        sb.append(lap.number()).append(',')
          .append(csv(lap.startTime())).append(',')
          .append(fmt(lap.totalTimeSeconds())).append(',')
          .append(fmt(lap.distanceMeters())).append(',')
          .append(fmt(lap.maximumSpeed())).append(',')
          .append(fmt(lap.calories())).append(',')
          .append(fmt(lap.avgHeartRate())).append(',')
          .append(fmt(lap.maxHeartRate())).append(',')
          .append(fmt(lap.avgCadence())).append(',')
          .append(csv(lap.intensity())).append(',')
          .append(csv(lap.triggerMethod())).append('\n');
    }

    private void appendTrackpoint(StringBuilder sb, Trackpoint tp, Instant activityStart) {
        Instant tpTime = parseInstant(tp.time());
        String elapsed = (tpTime != null && activityStart != null)
                ? String.valueOf(tpTime.getEpochSecond() - activityStart.getEpochSecond())
                : csv(tp.time());

        sb.append(tp.lapNumber()).append(',')
          .append(elapsed).append(',')
          .append(fmt(tp.altitudeMeters())).append(',')
          .append(fmt(tp.distanceMeters())).append(',')
          .append(fmt(tp.heartRateBpm())).append(',')
          .append(fmt(tp.cadence())).append(',')
          .append(fmt(tp.speed())).append(',')
          .append(fmt(tp.runCadence())).append(',')
          .append(fmt(tp.watts())).append(',')
          .append(csv(tp.sensorState())).append('\n');
    }

    private Instant parseInstant(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return null;
        try { return Instant.parse(timestamp); } catch (Exception e) { return null; }
    }

    private String csv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String fmt(Double value) {
        return value == null ? "" : value.toString();
    }

    private String fmt(Integer value) {
        return value == null ? "" : value.toString();
    }
}
