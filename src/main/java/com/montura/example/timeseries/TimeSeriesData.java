package com.montura.example.timeseries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public record TimeSeriesData(double x, double y, double z) {
    public static final Set<String> AVAILABLE_NAMES = new HashSet<>();
    public static final Map<String, Function<TimeSeriesData, Double>> TIMESERIES_DATA_MAPPER = new HashMap<>();

    static {
        AVAILABLE_NAMES.add("x");
        AVAILABLE_NAMES.add("y");
        AVAILABLE_NAMES.add("z");

        TIMESERIES_DATA_MAPPER.put("x", TimeSeriesData::x);
        TIMESERIES_DATA_MAPPER.put("y", TimeSeriesData::y);
        TIMESERIES_DATA_MAPPER.put("z", TimeSeriesData::z);
    }
}
