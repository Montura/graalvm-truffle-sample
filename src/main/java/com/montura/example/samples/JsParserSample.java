package com.montura.example.samples;

import com.montura.example.JsScriptParser;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class JsParserSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsParserSample.class.getName());

    public static void main(String[] args) {
        String code = """
            let a = x;
            function foo(a) {
                return a + 1;
            }
            var b = a * foo(2);
            let c = Math.sin(b) + y;
            output.z = z;
            """;

        Context ctx = Context.create("js");
        var scriptParser = new JsScriptParser(ctx);
        JsScriptParser.Result result = scriptParser.parse("rsi", code);

        LOGGER.info("Output names = {}", Arrays.toString(result.outputNames().toArray()));
        LOGGER.info("Using runtime names = {}", Arrays.toString(result.timeSeriesNames().toArray()));
    }
}
