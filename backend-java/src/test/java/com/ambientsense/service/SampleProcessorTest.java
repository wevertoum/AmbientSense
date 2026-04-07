package com.ambientsense.service;

import com.ambientsense.model.RawSensorReading;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SampleProcessorTest {

    private final SampleProcessor processor = new SampleProcessor();

    @Test
    void validReading() {
        var raw = new RawSensorReading(1000L, 22.0, 50.0, 40.0, "d1");
        var p = processor.process(raw, Instant.parse("2026-04-06T12:00:00Z"));
        assertThat(p.valid()).isTrue();
        assertThat(p.validationNotes()).isEmpty();
    }

    @Test
    void humidityOutOfRange() {
        var raw = new RawSensorReading(1000L, 22.0, 101.0, 40.0, "d1");
        var p = processor.process(raw, Instant.parse("2026-04-06T12:00:00Z"));
        assertThat(p.valid()).isFalse();
    }
}
