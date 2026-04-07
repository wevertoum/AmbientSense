package com.ambientsense.web;

import com.ambientsense.model.AlertRecord;
import com.ambientsense.model.IngestionState;
import com.ambientsense.service.MockJsonlIngestionService;
import com.ambientsense.service.SampleHistoryStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sensores", description = "Leituras e alertas (REST)")
public class SensorRestController {

    private static final Logger log = LoggerFactory.getLogger(SensorRestController.class);

    private final SampleHistoryStore historyStore;
    private final MockJsonlIngestionService ingestionService;

    public SensorRestController(SampleHistoryStore historyStore, MockJsonlIngestionService ingestionService) {
        this.historyStore = historyStore;
        this.ingestionService = ingestionService;
    }

    @Operation(summary = "Leitura atual", description = "Última amostra e alertas desta leitura.")
    @GetMapping("/samples/current")
    public ApiDtos.CurrentReadingResponse current() {
        var sample = historyStore.getLatestSample();
        var alerts = historyStore.getLatestAlerts();
        if (!alerts.isEmpty()) {
            for (AlertRecord a : alerts) {
                log.info(
                        "GET /api/v1/samples/current — alerta na resposta: metric={} severity={} observed={} limits=[{}, {}] msg={}",
                        a.metric(),
                        a.severity(),
                        a.observedValue(),
                        a.limitMin(),
                        a.limitMax(),
                        a.message());
            }
        }
        return new ApiDtos.CurrentReadingResponse(
                ApiDtos.ProcessedSampleDto.from(sample),
                alerts.stream().map(ApiDtos.AlertRecordDto::from).toList()
        );
    }

    @Operation(summary = "Histórico recente", description = "Últimas leituras com alertas; limit é limitado entre 1 e 500.")
    @GetMapping("/samples/recent")
    public ApiDtos.RecentReadingsResponse recent(@RequestParam(defaultValue = "64") int limit) {
        int safe = Math.min(Math.max(limit, 1), 500);
        List<ApiDtos.HistoryEntryDto> entries = historyStore.recent(safe).stream()
                .map(ApiDtos.HistoryEntryDto::from)
                .toList();
        return new ApiDtos.RecentReadingsResponse(entries);
    }

    @Operation(summary = "Estado do mock", description = "Caminho resolvido, totais, índice e se parou no EOF (STOP).")
    @GetMapping("/integration/state")
    public IngestionState integrationState() {
        return ingestionService.getState();
    }
}
