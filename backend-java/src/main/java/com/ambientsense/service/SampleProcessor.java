package com.ambientsense.service;

import com.ambientsense.model.ProcessedSample;
import com.ambientsense.model.RawSensorReading;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class SampleProcessor {

    private static final double TEMP_MIN = -55.0;
    private static final double TEMP_MAX = 125.0;

    public ProcessedSample process(RawSensorReading raw, Instant receivedAt) {
        List<String> notes = new ArrayList<>();
        if (raw == null) {
            return new ProcessedSample(0, receivedAt, 0, 0, 0, "", false, List.of("leitura nula"));
        }
        if (raw.timestamp() == null) {
            notes.add("timestamp ausente");
        }
        if (raw.temperature() == null) {
            notes.add("temperature ausente");
        }
        if (raw.humidity() == null) {
            notes.add("humidity ausente");
        }
        if (raw.luminosity() == null) {
            notes.add("luminosity ausente");
        }
        String device = raw.deviceId() != null ? raw.deviceId() : "";

        long ts = raw.timestamp() != null ? raw.timestamp() : 0L;
        double temp = raw.temperature() != null ? raw.temperature() : Double.NaN;
        double hum = raw.humidity() != null ? raw.humidity() : Double.NaN;
        double lux = raw.luminosity() != null ? raw.luminosity() : Double.NaN;

        if (raw.temperature() != null) {
            if (temp < TEMP_MIN || temp > TEMP_MAX) {
                notes.add("temperatura fora da faixa plausível do sensor");
            }
        }
        if (raw.humidity() != null) {
            if (hum < 0 || hum > 100) {
                notes.add("umidade fora de 0–100%");
            }
        }
        if (raw.luminosity() != null) {
            if (lux < 0 || lux > 100) {
                notes.add("luminosidade fora de 0–100");
            }
        }

        boolean valid = notes.isEmpty()
                && raw.timestamp() != null
                && raw.temperature() != null
                && raw.humidity() != null
                && raw.luminosity() != null;

        return new ProcessedSample(ts, receivedAt, temp, hum, lux, device, valid, notes);
    }
}
