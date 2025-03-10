package com.montura.example.timeseries;

import com.montura.example.JsContext;
import com.montura.example.JsScriptParser;
import jakarta.annotation.Nonnull;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.function.Function;

public class TimeSeriesProcessor {
    private static final int BUFFER_SIZE = 100;
    private final List<TimeSeriesToJsBufferMapper> mappedValues = new ArrayList<>();

    public TimeSeriesProcessor(
        @Nonnull JsContext ctx,
        @Nonnull JsScriptParser.Result result
    ) {
        Set<String> timeSeriesNames = result.timeSeriesNames();
        StringBuilder sb = new StringBuilder();
        timeSeriesNames.forEach(seriesName -> sb.append(defineTimeSeries(seriesName)));

        String initScript = sb.toString();
        Source script = Source.create("js", initScript);
        ctx.eval(script);

        timeSeriesNames.forEach(seriesName -> {
            Function<TimeSeriesData, Double> mapper = TimeSeriesData.TIMESERIES_DATA_MAPPER.get(seriesName);
            if (mapper != null) {
                mappedValues.add(new TimeSeriesToJsBufferMapper(mapper, ctx.get(seriesName)));
            } else {
                throw new IllegalStateException("There is no mapping for " + seriesName);
            }
        });
    }

    public void setCurrentValueAsDouble(int iterNumber, @Nonnull TimeSeriesData timeSeriesEvent) {
        int shiftedIdx = iterNumber % BUFFER_SIZE;
        for (TimeSeriesToJsBufferMapper value : mappedValues) {
            value.setArrayElement(shiftedIdx, timeSeriesEvent);
        }
    }

    public void close() {
        mappedValues.clear();
    }

    private static @Nonnull String defineTimeSeries(@Nonnull String seriesName) {
        return "const " + seriesName + " = new TimeSeries(" + BUFFER_SIZE + ");\n";
    }

    private static final class TimeSeriesToJsBufferMapper {
        private final Function<TimeSeriesData, Double> mapper;
        private final Value jsDataBuffer;

        public TimeSeriesToJsBufferMapper(
            @Nonnull Function<TimeSeriesData, Double> mapper,
            @Nonnull Value jsTimeSeriesObject
        ) {
            this.mapper = mapper;
            this.jsDataBuffer = jsTimeSeriesObject.getMember("dataBuffer");
        }

        public void setArrayElement(int idx, @Nonnull TimeSeriesData timeSeriesEvent) {
            Double value = mapper.apply(timeSeriesEvent);
            jsDataBuffer.setArrayElement(idx, value);
        }
    }
}
