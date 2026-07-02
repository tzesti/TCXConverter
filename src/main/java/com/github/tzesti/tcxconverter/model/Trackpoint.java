package com.github.tzesti.tcxconverter.model;

public record Trackpoint(
        int lapNumber,
        String time,
        Double latitude,
        Double longitude,
        Double altitudeMeters,
        Double distanceMeters,
        Integer heartRateBpm,
        Integer cadence,
        Double speed,
        Integer runCadence,
        Integer watts,
        String sensorState
) {}
