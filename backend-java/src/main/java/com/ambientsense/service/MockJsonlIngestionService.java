package com.ambientsense.service;

import com.ambientsense.config.AmbientsenseProperties;
import com.ambientsense.model.IngestionState;
import com.ambientsense.model.RawSensorReading;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MockJsonlIngestionService {

    private static final Logger log = LoggerFactory.getLogger(MockJsonlIngestionService.class);

    private final AmbientsenseProperties properties;
    private final JsonlLineParser parser;
    private final SampleProcessor processor;
    private final AlertEvaluator alertEvaluator;
    private final SampleHistoryStore historyStore;

    private List<String> lines = List.of();
    private final AtomicInteger nextIndex = new AtomicInteger(0);
    private Path resolvedPath;
    private volatile boolean eofStopped;

    public MockJsonlIngestionService(
            AmbientsenseProperties properties,
            JsonlLineParser parser,
            SampleProcessor processor,
            AlertEvaluator alertEvaluator,
            SampleHistoryStore historyStore
    ) {
        this.properties = properties;
        this.parser = parser;
        this.processor = processor;
        this.alertEvaluator = alertEvaluator;
        this.historyStore = historyStore;
    }

    @PostConstruct
    void init() {
        historyStore.configureMax(properties.getHistory().getMaxSamples());
        reloadSourceFile();
    }

    public void reloadSourceFile() {
        try {
            Path path = Path.of(properties.getMock().getJsonlPath()).toAbsolutePath().normalize();
            this.resolvedPath = path;
            if (!Files.isRegularFile(path)) {
                log.error("Arquivo JSONL não encontrado: {}", path);
                lines = List.of();
                return;
            }
            List<String> loaded = new ArrayList<>();
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line != null && !line.trim().isEmpty()) {
                    loaded.add(line.trim());
                }
            }
            lines = List.copyOf(loaded);
            nextIndex.set(0);
            eofStopped = false;
            log.info("Mock JSONL carregado: {} linhas de {}", lines.size(), path);
        } catch (Exception e) {
            log.error("Falha ao carregar JSONL", e);
            lines = List.of();
        }
    }

    @Scheduled(fixedDelayString = "${ambientsense.mock.tick-ms:1000}")
    public void tick() {
        if (lines.isEmpty() || eofStopped) {
            return;
        }
        int idx = nextIndex.getAndUpdate(i -> i + 1);
        if (idx >= lines.size()) {
            if (properties.getMock().getOnEof() == AmbientsenseProperties.OnEof.RESTART) {
                log.info("Fim do arquivo JSONL — reiniciando do início (on-eof=RESTART)");
                nextIndex.set(1);
                idx = 0;
            } else {
                log.info("Fim do arquivo JSONL — ingestão parada (on-eof=STOP)");
                eofStopped = true;
                return;
            }
        }
        String line = lines.get(idx);
        RawSensorReading raw = parser.parseLine(line);
        Instant now = Instant.now();
        var processed = processor.process(raw, now);
        var alerts = alertEvaluator.evaluate(processed);
        historyStore.append(processed, alerts);
    }

    public IngestionState getState() {
        int total = lines.size();
        int cursor = nextIndex.get();
        return new IngestionState(
                total == 0 ? 0 : Math.min(cursor, total),
                total,
                !lines.isEmpty(),
                eofStopped,
                resolvedPath != null ? resolvedPath.toString() : ""
        );
    }
}
