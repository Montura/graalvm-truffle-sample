package com.montura.example.samples;

import com.montura.example.JSScript;
import com.montura.example.timeseries.TimeSeriesData;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("Duplicates")
public class MultiThreadedSample {
    private static final AtomicInteger THREAD_ID = new AtomicInteger();
    private static final ExecutorService SCRIPT_EXECUTOR = Executors.newFixedThreadPool(2, myThreadFactory());
    private static final LinkedBlockingQueue<JSScript> SCRIPT_MT_BLOCKING_QUEUE = new LinkedBlockingQueue<>();
    private static final List<TimeSeriesData> TIME_SERIES_DATA = generateTimeSeries(500);
    private static final Map<String, String> POLYGLOT_PROPERTIES = new HashMap<>();
    private static final int TOTAL_SCRIPTS = 1000;
    public static final String SOURCE_NAME = "sourceCode";

    public static void main(String[] args) throws InterruptedException {
        String content = """
            let res = moving_average(x, 50);
            output.res = res;
            """;
        addSandboxOptions();
        for (int id = 0; id < TOTAL_SCRIPTS; id++) {
            SCRIPT_MT_BLOCKING_QUEUE.offer(new JSScript(SOURCE_NAME, content, POLYGLOT_PROPERTIES));
        }
        long timeToWork = 100_000_000;
        while (timeToWork-- > 0) {
            SCRIPT_EXECUTOR.execute(() -> {
                var s = SCRIPT_MT_BLOCKING_QUEUE.poll();
                s.run(TIME_SERIES_DATA);
                SCRIPT_MT_BLOCKING_QUEUE.offer(s);
            });
            Thread.sleep(100);
        }
        clearResourcesAndShutdown();
    }

    private static void clearResourcesAndShutdown() {
        try {
            SCRIPT_EXECUTOR.shutdown();
        } catch (Throwable t) {
            System.err.println("Failed to shutdown script runner executor:\n\t" + t.getMessage());
        }
        SCRIPT_MT_BLOCKING_QUEUE.forEach(JSScript::close);
        SCRIPT_MT_BLOCKING_QUEUE.clear();
    }

    private static List<TimeSeriesData> generateTimeSeries(int count) {
        ArrayList<TimeSeriesData> result = new ArrayList<>(count);
        for (int i = count - 1; i > 0; --i) {
            result.add(new TimeSeriesData(i, i % 2 + 1, i % 3 + 1));
        }
        return result;
    }

    private static ThreadFactory myThreadFactory() {
        return r -> new Thread(r, "ScriptExecutor-Thread-" + THREAD_ID.incrementAndGet());
    }

    private static void addSandboxOptions() {
        POLYGLOT_PROPERTIES.put("sandbox.MaxCPUTime", "500ms");
        POLYGLOT_PROPERTIES.put("sandbox.MaxHeapMemory", "50MB");
    }
}
