package com.huawei.devkit.pipeline.parse;

import com.huawei.devkit.pipeline.bo.JmeterRT;
import com.huawei.devkit.pipeline.bo.JmeterResult;
import com.huawei.devkit.pipeline.bo.JmeterTPS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JmeterResultParser {
    private static final Logger logger = LogManager.getLogger(JmeterResultParser.class);

    private int timeStampIndex;

    private int latencyIndex;

    private int responseCodeIndex;

    private long latencyAverageSummary;

    private long tpsSummary;

    private List<JmeterRT> rtList;

    private List<JmeterRT> frtList;

    private List<JmeterTPS> tpsList;

    public long getLatencyAverageSummary() {
        return latencyAverageSummary;
    }

    public long getTpsSummary() {
        return tpsSummary;
    }

    public List<JmeterRT> getRtList() {
        return rtList;
    }

    public List<JmeterRT> getFrtList() {
        return frtList;
    }

    public List<JmeterTPS> getTpsList() {
        return tpsList;
    }

    public void parse(String resultPath) throws Exception {
        Path path = Paths.get(resultPath);
        if (!Files.exists(path)) {
            throw new Exception("the file not exist");
        }
        List<JmeterResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(resultPath))) {
            String headers = reader.readLine();
            this.parseHeader(headers);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                results.add(new JmeterResult(Long.parseLong(fields[timeStampIndex]),
                        Integer.parseInt(fields[responseCodeIndex]), Integer.parseInt(fields[latencyIndex])));
            }
            calcTPSAndRT(results);
        }
    }

    private void calcTPSAndRT(List<JmeterResult> results) {
        if (Objects.isNull(results) || results.isEmpty()) {
            return;
        }
        JmeterResult result = results.get(0);
        long start = result.getStartTime();
        long startTime = start;
        long end = start + 1000;
        int tps = 1;
        int latencyTotalPerSec = result.getLatency();
        int latencyFailPerSec = 0;
        if (result.getResponseCode() >= 300) {
            latencyFailPerSec = result.getLatency();
        }
        long latencyTotal = result.getLatency();
        long tpsTotal = 1;

        for (int i = 1; i < results.size(); i++) {
            JmeterResult item = results.get(i);
            tpsTotal += 1;
            latencyTotal += item.getLatency();
            if (item.getStartTime() < end) {
                latencyTotalPerSec += item.getLatency();
                tps += 1;
                if (item.getResponseCode() >= 300) {
                    latencyFailPerSec += item.getLatency();
                }
            } else {
                rtList.add(new JmeterRT(start, latencyTotalPerSec / (double) tps));
                frtList.add(new JmeterRT(start, latencyFailPerSec / (double) tps));
                tpsList.add(new JmeterTPS(start, tps));
                start = item.getStartTime();
                end = start + 1000;
                latencyTotalPerSec = item.getLatency();
                tps = 1;
                latencyFailPerSec = 0;
                if (item.getResponseCode() >= 300) {
                    latencyFailPerSec += item.getLatency();
                }
            }
        }
        long endTime = end;
        tpsSummary = tpsTotal / (endTime / 1000 - startTime / 1000);
        latencyAverageSummary = latencyTotal / tpsTotal;
        rtList.add(new JmeterRT(start, latencyTotalPerSec / (double) tps));
        frtList.add(new JmeterRT(start, latencyFailPerSec / (double) tps));
        tpsList.add(new JmeterTPS(start, tps));
    }

    private void parseHeader(String headersCombined) {
        String[] header = headersCombined.split(",");
        for (int i = 0; i < header.length; i++) {
            if ("latency".equalsIgnoreCase(header[i])) {
                latencyIndex = i;
            } else if ("timestamp".equalsIgnoreCase(header[i])) {
                timeStampIndex = i;
            } else if ("responseCode".equalsIgnoreCase(header[i])) {
                responseCodeIndex = i;
            }
        }
    }
}
