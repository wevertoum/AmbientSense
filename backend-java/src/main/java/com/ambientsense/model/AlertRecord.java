package com.ambientsense.model;

import java.time.Instant;

public record AlertRecord(
        String severity,
        String message,
        Instant triggeredAt,
        MetricKind metric,
        double observedValue,
        Double limitMin,
        Double limitMax
) {}
