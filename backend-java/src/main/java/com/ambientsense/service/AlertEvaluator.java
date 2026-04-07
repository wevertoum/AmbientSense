package com.ambientsense.service;

import com.ambientsense.config.AmbientsenseProperties;
import com.ambientsense.model.AlertRecord;
import com.ambientsense.model.MetricKind;
import com.ambientsense.model.ProcessedSample;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class AlertEvaluator {

    private final AmbientsenseProperties props;

    public AlertEvaluator(AmbientsenseProperties props) {
        this.props = props;
    }

    public List<AlertRecord> evaluate(ProcessedSample sample) {
        List<AlertRecord> out = new ArrayList<>();
        if (sample == null || !sample.valid()) {
            return out;
        }
        Instant t = sample.serverReceivedAt() != null ? sample.serverReceivedAt() : Instant.now();
        check(out, t, MetricKind.TEMPERATURE, sample.temperatureC(), props.getAlerts().getTemperatureC());
        check(out, t, MetricKind.HUMIDITY, sample.humidityPercent(), props.getAlerts().getHumidityPercent());
        check(out, t, MetricKind.LUMINOSITY, sample.luminosityPercent(), props.getAlerts().getLuminosityPercent());
        return out;
    }

    private static void check(
            List<AlertRecord> out,
            Instant triggeredAt,
            MetricKind metric,
            double value,
            AmbientsenseProperties.LimitRule rule
    ) {
        Double min = rule.getMin();
        Double max = rule.getMax();
        if (min != null && value < min) {
            out.add(new AlertRecord(
                    "WARN",
                    metric.name() + " abaixo do mínimo configurado",
                    triggeredAt,
                    metric,
                    value,
                    min,
                    max
            ));
        }
        if (max != null && value > max) {
            out.add(new AlertRecord(
                    "WARN",
                    metric.name() + " acima do máximo configurado",
                    triggeredAt,
                    metric,
                    value,
                    min,
                    max
            ));
        }
    }
}
