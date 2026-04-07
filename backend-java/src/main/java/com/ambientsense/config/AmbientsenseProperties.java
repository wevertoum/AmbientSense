package com.ambientsense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ambientsense")
public class AmbientsenseProperties {

    private Mock mock = new Mock();
    private Alerts alerts = new Alerts();
    private History history = new History();

    public Mock getMock() {
        return mock;
    }

    public Alerts getAlerts() {
        return alerts;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public static class Mock {
        private String jsonlPath = "data/sample-output.jsonl";
        private long tickMs = 1000;
        private OnEof onEof = OnEof.RESTART;

        public String getJsonlPath() {
            return jsonlPath;
        }

        public void setJsonlPath(String jsonlPath) {
            this.jsonlPath = jsonlPath;
        }

        public long getTickMs() {
            return tickMs;
        }

        public void setTickMs(long tickMs) {
            this.tickMs = tickMs;
        }

        public OnEof getOnEof() {
            return onEof;
        }

        public void setOnEof(OnEof onEof) {
            this.onEof = onEof;
        }
    }

    public enum OnEof {
        RESTART,
        STOP
    }

    public static class Alerts {
        private LimitRule temperatureC = new LimitRule(18.0, 30.0);
        private LimitRule humidityPercent = new LimitRule(20.0, 80.0);
        private LimitRule luminosityPercent = new LimitRule(0.0, 100.0);

        public LimitRule getTemperatureC() {
            return temperatureC;
        }

        public void setTemperatureC(LimitRule temperatureC) {
            this.temperatureC = temperatureC;
        }

        public LimitRule getHumidityPercent() {
            return humidityPercent;
        }

        public void setHumidityPercent(LimitRule humidityPercent) {
            this.humidityPercent = humidityPercent;
        }

        public LimitRule getLuminosityPercent() {
            return luminosityPercent;
        }

        public void setLuminosityPercent(LimitRule luminosityPercent) {
            this.luminosityPercent = luminosityPercent;
        }
    }

    public static class LimitRule {
        private Double min;
        private Double max;

        public LimitRule() {}

        public LimitRule(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        public Double getMin() {
            return min;
        }

        public void setMin(Double min) {
            this.min = min;
        }

        public Double getMax() {
            return max;
        }

        public void setMax(Double max) {
            this.max = max;
        }
    }

    public static class History {
        private int maxSamples = 500;

        public int getMaxSamples() {
            return maxSamples;
        }

        public void setMaxSamples(int maxSamples) {
            this.maxSamples = maxSamples;
        }
    }
}
