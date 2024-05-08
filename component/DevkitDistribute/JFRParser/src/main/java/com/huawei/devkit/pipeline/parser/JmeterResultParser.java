package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.JmeterRT;
import com.huawei.devkit.pipeline.bo.JmeterReportSummary;
import com.huawei.devkit.pipeline.bo.JmeterResult;
import com.huawei.devkit.pipeline.bo.JmeterTPS;
import com.huawei.devkit.pipeline.utils.JmeterResultTransfer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JmeterResultParser {
    private static final Logger logger = LogManager.getLogger(JmeterResultParser.class);


    private JmeterReportSummary summary;

    private List<JmeterRT> rtList;

    private List<JmeterRT> frtList;

    private List<JmeterTPS> tpsList;

    public JmeterResultParser() {
        this.summary = new JmeterReportSummary();
        this.rtList = new ArrayList<>(1000);
        this.frtList = new ArrayList<>(1000);
        this.tpsList = new ArrayList<>(1000);
    }

    public static void parse(String resultPath) throws Exception {
        List<JmeterResult> results = JmeterResultTransfer.transfer(resultPath);
        JmeterResultParser parser = new JmeterResultParser();
        parser.calcTPSAndRT(results);
        Map<String, List<JmeterResult>> map = results.stream().collect(Collectors.groupingBy(JmeterResult::getLabel));
        for (Map.Entry<String, List<JmeterResult>> entry : map.entrySet()) {
            JmeterResultParser parserPer = new JmeterResultParser();
            parserPer.calcTPSAndRT(results);
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
        int count = 1;
        int latencyTotalPerSec = result.getLatency();
        int latencyFailPerSec = 0;
        if (result.getResponseCode() >= 300) {
            summary.failSamplesIncrease();
            latencyFailPerSec = result.getLatency();
        }
        long latencyTotal = result.getLatency();
        summary.samplesIncrease();

        for (int i = 1; i < results.size(); i++) {
            JmeterResult item = results.get(i);
            summary.samplesIncrease();
            latencyTotal += item.getLatency();
            if (item.getStartTime() < end) {
                latencyTotalPerSec += item.getLatency();
                count++;
            } else {
                // re init
                rtList.add(new JmeterRT(start, latencyTotalPerSec / (double) count));
                frtList.add(new JmeterRT(start, latencyFailPerSec / (double) count));
                tpsList.add(new JmeterTPS(start, count));
                do {
                    start = start + 1000;
                    end = start + 1000;
                } while (item.getStartTime() < end);
                latencyTotalPerSec = item.getLatency();
                count = 1;
                latencyFailPerSec = 0;
            }
            if (item.getResponseCode() >= 300) {
                summary.failSamplesIncrease();
                latencyFailPerSec += item.getLatency();
            }
        }
        long endTime = end;
        summary.setThroughput(summary.getSamples() / (endTime / 1000 - startTime / 1000));
        summary.setAverageLatency(latencyTotal / (double) summary.getSamples());
        rtList.add(new JmeterRT(start, latencyTotalPerSec / (double) count));
        frtList.add(new JmeterRT(start, latencyFailPerSec / (double) count));
        tpsList.add(new JmeterTPS(start, count));
        this.filledSummary(results);
    }

    public JmeterReportSummary getSummary() {
        return summary;
    }

    public void setSummary(JmeterReportSummary summary) {
        this.summary = summary;
    }

    public List<JmeterRT> getRtList() {
        return rtList;
    }

    public void setRtList(List<JmeterRT> rtList) {
        this.rtList = rtList;
    }

    public List<JmeterRT> getFrtList() {
        return frtList;
    }

    public void setFrtList(List<JmeterRT> frtList) {
        this.frtList = frtList;
    }

    public List<JmeterTPS> getTpsList() {
        return tpsList;
    }

    public void setTpsList(List<JmeterTPS> tpsList) {
        this.tpsList = tpsList;
    }

    private void filledSummary(List<JmeterResult> results) {
        List<JmeterResult> jmeterResults = results.stream()
                .sorted(Comparator.comparingLong(JmeterResult::getLatency)).collect(Collectors.toList());
        JmeterResult result = jmeterResults.get(0);
        summary.setMinLatency(result.getLatency());
        summary.setMaxLatency(jmeterResults.get(jmeterResults.size() - 1).getLatency());
        int position50 = jmeterResults.size() * 50 / 100 - 1;
        summary.setMedian(jmeterResults.get(position50).getLatency());
        int position90 = jmeterResults.size() * 90 / 100 - 1;
        summary.setLatency90(jmeterResults.get(position90).getLatency());
        int position95 = jmeterResults.size() * 95 / 100 - 1;
        summary.setLatency95(jmeterResults.get(position95).getLatency());
        int position99 = jmeterResults.size() * 99 / 100 - 1;
        summary.setLatency99(jmeterResults.get(position99).getLatency());
    }
}
