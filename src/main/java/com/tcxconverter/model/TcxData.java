package com.tcxconverter.model;

import java.util.List;

public record TcxData(
        List<Activity> activities,
        String authorName
) {}
