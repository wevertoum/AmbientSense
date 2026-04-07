package com.ambientsense.service;

import com.ambientsense.model.AlertRecord;
import com.ambientsense.model.ProcessedSample;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SampleHistoryStore {

    private final ArrayList<HistoryEntry> buffer = new ArrayList<>();
    private int maxSize = 500;

    public synchronized void configureMax(int max) {
        this.maxSize = Math.max(1, max);
        trim();
    }

    public synchronized void append(ProcessedSample sample, List<AlertRecord> alerts) {
        buffer.add(new HistoryEntry(sample, List.copyOf(alerts)));
        trim();
    }

    private void trim() {
        while (buffer.size() > maxSize) {
            buffer.remove(0);
        }
    }

    public synchronized ProcessedSample getLatestSample() {
        if (buffer.isEmpty()) {
            return null;
        }
        return buffer.get(buffer.size() - 1).sample();
    }

    public synchronized List<AlertRecord> getLatestAlerts() {
        if (buffer.isEmpty()) {
            return List.of();
        }
        return buffer.get(buffer.size() - 1).alerts();
    }

    public synchronized List<HistoryEntry> recent(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        int n = Math.min(limit, buffer.size());
        int from = buffer.size() - n;
        return Collections.unmodifiableList(new ArrayList<>(buffer.subList(from, buffer.size())));
    }

    public record HistoryEntry(ProcessedSample sample, List<AlertRecord> alerts) {}
}
