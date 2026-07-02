package com.github.tzesti.tcxconverter.model;

import java.util.List;

public record Lap(
        int number,
        String startTime,
        Double totalTimeSeconds,
        Double distanceMeters,
        Double maximumSpeed,
        Integer calories,
        Integer avgHeartRate,
        Integer maxHeartRate,
        String intensity,
        Integer avgCadence,
        String triggerMethod,
        List<Trackpoint> trackpoints
) {}
