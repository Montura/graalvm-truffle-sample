package com.montura.example;

import jakarta.annotation.Nonnull;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

public class JsContext {
    private static final String JS = "js";
    private static final Source INIT_JS_SOURCE;
    private static final String INIT_JS = "init_js.js";
    private final Value contextBindings;
    private final Context context;
    private final JsScriptParser jsParser;

    static {
        INIT_JS_SOURCE = getSource(INIT_JS);
    }

    JsContext(@Nonnull Map<String, String> properties) {
        context = Context.newBuilder(JS)
            .allowHostClassLookup(JsContext::classFilter)
            .allowHostAccess(HostAccess.ALL)
            .options(properties)
            .build();

        jsParser = new JsScriptParser(context);
        context.eval(INIT_JS_SOURCE);

        contextBindings = context.getBindings(JS);
    }

    public @Nonnull JsScriptParser.Result parseScript(
        @Nonnull String sourceName,
        @Nonnull String scriptCode
    ) {
        return jsParser.parse(sourceName, scriptCode);
    }

    public void resetLimits() {
        context.resetLimits();
    }

    public void close() {
        context.close();
    }

    public void eval(@Nonnull Source source) {
        context.eval(source);
    }

    public @Nonnull Value get(String key) {
        return contextBindings.getMember(key);
    }

    private static boolean classFilter(String classFilter) {
        return classFilter.contains(JsContext.class.getSimpleName());
    }

    private static Source getSource(String fileName) {
        ClassLoader classLoader = JsContext.class.getClassLoader();
        try (InputStreamReader code = new InputStreamReader(
            Objects.requireNonNull(classLoader.getResourceAsStream(fileName))))
        {
            return Source.newBuilder(JS, code, fileName).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
