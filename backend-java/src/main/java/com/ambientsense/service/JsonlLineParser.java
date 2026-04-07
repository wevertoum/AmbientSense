package com.ambientsense.service;

import com.ambientsense.model.RawSensorReading;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonlLineParser {

    private static final Logger log = LoggerFactory.getLogger(JsonlLineParser.class);

    private final ObjectMapper objectMapper;

    public JsonlLineParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RawSensorReading parseLine(String line) {
        try {
            return objectMapper.readValue(line, RawSensorReading.class);
        } catch (Exception e) {
            log.warn("JSON inválido ignorado: {} — {}", truncate(line), e.getMessage());
            return null;
        }
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() > 120 ? t.substring(0, 120) + "…" : t;
    }
}
