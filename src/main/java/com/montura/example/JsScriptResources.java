package com.montura.example;

import com.montura.example.timeseries.TimeSeriesData;
import com.montura.example.timeseries.TimeSeriesProcessor;
import jakarta.annotation.Nonnull;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.util.Arrays;
import java.util.Set;

public final class JsScriptResources {
    private static final String EXECUTABLE_JS_METHOD_NAME = "code";
    private static final String ITERATION_BUFFER_JS_NAME = "ITERATION_BUFFER";
    private static final String OUTPUTS_JS_NAME = "output";

    private final TimeSeriesProcessor timeSeriesProcessor;
    private final Value currentIterationBuffer;
    private final Value jsCodeToEval;
    private final Value outsObject;
    private final Set<String> outputNames;

    private int currentIterationNumber = 0;

    JsScriptResources(
        @Nonnull JsContext ctx,
        @Nonnull String sourceName,
        @Nonnull String sourceCode
    ) {
        JsScriptParser.Result parsedResult = ctx.parseScript(sourceName, sourceCode);
        timeSeriesProcessor = new TimeSeriesProcessor(ctx, parsedResult);
        outputNames = parsedResult.outputNames();

        Source code = wrapUserCodeInFunction(sourceCode);
        ctx.eval(code);
        jsCodeToEval = ctx.get(EXECUTABLE_JS_METHOD_NAME);
        jsCodeToEval.executeVoid();

        currentIterationBuffer = ctx.get(ITERATION_BUFFER_JS_NAME);
        outsObject = getObjectWithProps(ctx, OUTPUTS_JS_NAME, outputNames);
    }

    public void close() {
        timeSeriesProcessor.close();
    }

    public void incrementIterationNumber() {
        ++currentIterationNumber;
    }

    public void setBufferValue(@Nonnull TimeSeriesData event) {
        currentIterationBuffer.setArrayElement(0, currentIterationNumber);
        timeSeriesProcessor.setCurrentValueAsDouble(currentIterationNumber, event);
    }

    public @Nonnull Value getJsCodeToEval() {
        return jsCodeToEval;
    }

    public @Nonnull Set<String> getOutputNames() {
        return outputNames;
    }

    public @Nonnull Value getOutputValue(String key) {
        return outsObject.getMember(key);
    }

    private static Source wrapUserCodeInFunction(String scriptCode) {
        return Source.create("js", "function " + EXECUTABLE_JS_METHOD_NAME + "() {\n" + scriptCode + "}\n");
    }

    private static Value getObjectWithProps(JsContext ctx, String name, Set<String> propertyNames) {
        var jsObject = ctx.get(name);
        Set<String> memberKeys = jsObject.getMemberKeys();
        if (!memberKeys.containsAll(propertyNames)) {
            String runtimeKeys = Arrays.toString(memberKeys.toArray());
            String parsedKeys = Arrays.toString(propertyNames.toArray());
            throw new IllegalStateException("Object properties in runtime are: " + runtimeKeys +
                ", but parsed object properties are: " + parsedKeys);
        }
        return jsObject;
    }
}
