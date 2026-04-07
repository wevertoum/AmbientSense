package com.ambientsense.model;

public record IngestionState(
        int lineIndex,
        int totalLines,
        boolean sourceReady,
        boolean stoppedAtEof,
        String jsonlPathResolved
) {}
