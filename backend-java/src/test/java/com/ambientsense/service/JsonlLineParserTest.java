package com.ambientsense.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonlLineParserTest {

    private final JsonlLineParser parser = new JsonlLineParser(new ObjectMapper());

    @Test
    void parsesValidLine() {
        var raw = parser.parseLine(
                "{\"timestamp\":1000,\"temperature\":24.7,\"humidity\":30,\"luminosity\":0,\"deviceId\":\"ambient-sense-01\"}"
        );
        assertThat(raw).isNotNull();
        assertThat(raw.timestamp()).isEqualTo(1000L);
        assertThat(raw.temperature()).isEqualTo(24.7);
        assertThat(raw.deviceId()).isEqualTo("ambient-sense-01");
    }

    @Test
    void invalidJsonReturnsNull() {
        assertThat(parser.parseLine("not json")).isNull();
    }
}
