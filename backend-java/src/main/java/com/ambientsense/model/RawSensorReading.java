package com.ambientsense.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawSensorReading(
        Long timestamp,
        Double temperature,
        Double humidity,
        Double luminosity,
        String deviceId
) {}
