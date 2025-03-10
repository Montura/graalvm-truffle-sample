package com.montura.example.samples;

import com.montura.example.timeseries.TimeSeriesData;
import com.montura.example.JSScript;
import com.montura.example.timeseries.TimeSeriesResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsSample {
    public static void main(String[] args) {
        String content = """
            let res = moving_average(x, 3)
            output.res = res
            """;

        var s = new JSScript("test", content, Collections.emptyMap());
        var timeSeriesNameList = generateTimeSeries(10);

        List<TimeSeriesResult> results = s.run(timeSeriesNameList);
        for (TimeSeriesResult result : results) {
            System.out.println(result.get("res").value());
        }
        s.close();
    }

    private static List<TimeSeriesData> generateTimeSeries(int count) {
        ArrayList<TimeSeriesData> result = new ArrayList<>(count);
        for (int i = count - 1; i > 0; --i) {
            result.add(new TimeSeriesData(i, i % 2 + 1, i % 3 + 1));
        }
        return result;
    }
}
