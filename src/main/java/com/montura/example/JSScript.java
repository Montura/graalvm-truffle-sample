package com.montura.example;

import com.montura.example.timeseries.TimeSeriesData;
import com.montura.example.timeseries.TimeSeriesResult;
import jakarta.annotation.Nonnull;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JSScript implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSScript.class.getName());

    private final JsScriptResources scriptResources;
    private final JsContext ctx;
    private final Value userCodeToEval;

    public JSScript(
        @Nonnull String sourceName,
        @Nonnull String sourceCode,
        @Nonnull Map<String, String> polyglotProperties
    ) {
        ctx = new JsContext(polyglotProperties);
        scriptResources = new JsScriptResources(ctx, sourceName, sourceCode);
        userCodeToEval = scriptResources.getJsCodeToEval();
        ctx.resetLimits();
    }

    @Override
    public void close() {
        scriptResources.close();
        ctx.close();
    }

    public @Nonnull List<TimeSeriesResult> run(@Nonnull List<TimeSeriesData> events) {
        List<TimeSeriesResult> processedEvents = new ArrayList<>();
        for (TimeSeriesData currentData : events) {
            processedEvents.add(process(currentData));
        }
        return processedEvents;
    }

    private @Nonnull TimeSeriesResult process(@Nonnull TimeSeriesData event) {
        try {
            scriptResources.setBufferValue(event);

            ctx.resetLimits();
            // execute computation
            userCodeToEval.executeVoid();

            scriptResources.incrementIterationNumber();

            // retrieve next result
            Map<String, TimeSeriesResult.Result> iterationResult = new HashMap<>();
            for (String key : scriptResources.getOutputNames()) {
                Object result = getResultAsDouble(scriptResources.getOutputValue(key));
                iterationResult.put(key, new TimeSeriesResult.Result(key, result));
            }
            return new TimeSeriesResult(iterationResult);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    private static Object getResultAsDouble(Value element) {
        if (element.isNumber()) {
            return element.asDouble();
        } else {
            throw new IllegalStateException("Unknown type:" + element.asString());
        }
    }
}
