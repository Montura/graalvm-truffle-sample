package com.montura.example.timeseries;

import jakarta.annotation.Nonnull;

import java.util.Map;

public class TimeSeriesResult {
    private final Map<String, Result> result;

    public TimeSeriesResult(@Nonnull Map<String, Result> result) {
        this.result = result;
    }

    public Result get(String name) {
        return result.get(name);
    }

    public record Result (String name, Object value) {}
}
