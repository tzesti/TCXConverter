package com.github.tzesti.tcxconverter.model;

import java.util.List;

public record Activity(
        String sport,
        String id,
        List<Lap> laps,
        String creatorName
) {}
