package com.ambientsense.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ProcessedSample(
        long timestampMillis,
        Instant serverReceivedAt,
        double temperatureC,
        double humidityPercent,
        double luminosityPercent,
        String deviceId,
        boolean valid,
        List<String> validationNotes
) {
    public ProcessedSample {
        validationNotes = validationNotes == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(validationNotes));
    }
}
