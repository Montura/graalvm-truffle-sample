package com.montura.example;

import com.montura.example.timeseries.TimeSeriesData;
import com.oracle.js.parser.*;
import com.oracle.js.parser.ir.*;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import jakarta.annotation.Nonnull;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class JsScriptParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsScriptParser.class.getName());
    private final ScriptEnvironment env;

    public JsScriptParser(@Nonnull Context jsContext) {
        env = makeScriptEnvironment(jsContext);
    }

    public Result parse(@Nonnull String sourceName, @Nonnull String scriptCode) {
        Source name = Source.sourceFor(sourceName, scriptCode);
        var parser = new Parser(env, name, new ErrorManager.ThrowErrorManager(), env.isStrict());
        var scriptVisitor = new JsScriptVisitor();
        try {
            FunctionNode parse = parser.parse();
            parse.accept(scriptVisitor);
        } catch (ParserException e) {
            LOGGER.error(e.getMessage());
        }
        return new Result(scriptVisitor.outputNames, scriptVisitor.timeSeriesNames);
    }

    private static final class JsScriptVisitor extends NodeVisitor<LexicalContext> {
        private static final String OUTPUT_PREFIX = "output";

        public final Set<String> outputNames = new HashSet<>();
        public final Set<String> timeSeriesNames = new HashSet<>();

        private String lastPropertyAccess = null;

        public JsScriptVisitor() {
            super(new LexicalContext());
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            lastPropertyAccess = accessNode.getProperty();
            return super.enterAccessNode(accessNode);
        }

        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            String identifier = identNode.getName();
            if (OUTPUT_PREFIX.equals(identifier) && lastPropertyAccess != null) {
                outputNames.add(lastPropertyAccess);
            } else if (TimeSeriesData.AVAILABLE_NAMES.contains(identifier)) {
                timeSeriesNames.add(identifier);
            }
            return this.enterDefault(identNode);
        }

        @Override
        public Node leaveAccessNode(AccessNode accessNode) {
            lastPropertyAccess = null;
            return super.leaveAccessNode(accessNode);
        }
    }

    public record Result(Set<String> outputNames, Set<String> timeSeriesNames) {
    }

    private static @Nonnull ScriptEnvironment makeScriptEnvironment(@Nonnull Context context) {
        var jsContext = JavaScriptLanguage.getJSContext(context);
        var parserOptions = jsContext.getParserOptions();

        // Copy-paste from com.oracle.truffle.js.parser.GraalJSParserHelper.makeScriptEnvironment
        var builder = ScriptEnvironment.builder();
        builder.strict(parserOptions.strict());
        int ecmaScriptVersion = parserOptions.ecmaScriptVersion();
        if (ecmaScriptVersion == 16) {
            ecmaScriptVersion = Integer.MAX_VALUE;
        }

        builder.ecmaScriptVersion(ecmaScriptVersion);
        builder.emptyStatements(parserOptions.emptyStatements());
        builder.syntaxExtensions(parserOptions.syntaxExtensions());
        builder.scripting(parserOptions.scripting());
        builder.shebang(parserOptions.shebang());
        builder.constAsVar(parserOptions.constAsVar());
        builder.allowBigInt(parserOptions.allowBigInt());
        builder.annexB(parserOptions.annexB());
        builder.classFields(parserOptions.classFields());
        builder.importAttributes(parserOptions.importAttributes());
        builder.privateFieldsIn(parserOptions.privateFieldsIn());
        builder.topLevelAwait(parserOptions.topLevelAwait());
        builder.v8Intrinsics(parserOptions.v8Intrinsics());
        if (parserOptions.functionStatementError()) {
            builder.functionStatementBehavior(ScriptEnvironment.FunctionStatementBehavior.ERROR);
        } else {
            builder.functionStatementBehavior(ScriptEnvironment.FunctionStatementBehavior.ACCEPT);
        }

        if (parserOptions.dumpOnError()) {
            builder.dumpOnError(new PrintWriter(System.err, true));
        }

        return builder.build();
    }
}
