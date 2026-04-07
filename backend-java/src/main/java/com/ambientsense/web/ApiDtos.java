package com.ambientsense.web;

import com.ambientsense.model.AlertRecord;
import com.ambientsense.model.ProcessedSample;
import com.ambientsense.service.SampleHistoryStore;

import java.util.List;

public final class ApiDtos {

    private ApiDtos() {}

    public record ProcessedSampleDto(
            long timestampMillis,
            String serverReceivedAt,
            double temperatureC,
            double humidityPercent,
            double luminosityPercent,
            String deviceId,
            boolean valid,
            List<String> validationNotes
    ) {
        static ProcessedSampleDto from(ProcessedSample s) {
            if (s == null) {
                return null;
            }
            return new ProcessedSampleDto(
                    s.timestampMillis(),
                    s.serverReceivedAt() != null ? s.serverReceivedAt().toString() : null,
                    s.temperatureC(),
                    s.humidityPercent(),
                    s.luminosityPercent(),
                    s.deviceId(),
                    s.valid(),
                    s.validationNotes()
            );
        }
    }

    public record AlertRecordDto(
            String severity,
            String message,
            String triggeredAt,
            String metric,
            double observedValue,
            Double limitMin,
            Double limitMax
    ) {
        static AlertRecordDto from(AlertRecord a) {
            return new AlertRecordDto(
                    a.severity(),
                    a.message(),
                    a.triggeredAt() != null ? a.triggeredAt().toString() : null,
                    a.metric().name(),
                    a.observedValue(),
                    a.limitMin(),
                    a.limitMax()
            );
        }
    }

    public record CurrentReadingResponse(ProcessedSampleDto sample, List<AlertRecordDto> alerts) {}

    public record HistoryEntryDto(ProcessedSampleDto sample, List<AlertRecordDto> alerts) {
        static HistoryEntryDto from(SampleHistoryStore.HistoryEntry e) {
            return new HistoryEntryDto(
                    ProcessedSampleDto.from(e.sample()),
                    e.alerts().stream().map(AlertRecordDto::from).toList()
            );
        }
    }

    public record RecentReadingsResponse(List<HistoryEntryDto> entries) {}
}
