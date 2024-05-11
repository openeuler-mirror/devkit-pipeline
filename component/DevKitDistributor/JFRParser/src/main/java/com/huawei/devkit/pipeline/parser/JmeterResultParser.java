package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.JmeterRT;
import com.huawei.devkit.pipeline.bo.JmeterReportSummary;
import com.huawei.devkit.pipeline.bo.JmeterResult;
import com.huawei.devkit.pipeline.bo.JmeterTPS;
import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import com.huawei.devkit.pipeline.constants.JFRConstants;
import com.huawei.devkit.pipeline.utils.JmeterResultTransfer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.huawei.devkit.pipeline.constants.JFRConstants.TOTAL_LABEL;

public class JmeterResultParser {
    private static final Logger logger = LogManager.getLogger(JmeterResultParser.class);

    private static final int MIN_ERROR_CODE = 400;
    private static final int PERCENT_50 = 50;
    private static final int PERCENT_90 = 90;
    private static final int PERCENT_95 = 95;
    private static final int PERCENT_99 = 99;

    private JmeterReportSummary summary;

    private List<JmeterRT> rtList;

    private List<JmeterRT> frtList;

    private List<JmeterTPS> tpsList;

    public JmeterResultParser(String label) {
        this.summary = new JmeterReportSummary(label);
        this.rtList = new ArrayList<>(1000);
        this.frtList = new ArrayList<>(1000);
        this.tpsList = new ArrayList<>(1000);
    }

    public static void parse(String resultPath, PerformanceTestResult result) throws Exception {
        List<JmeterResult> results = JmeterResultTransfer.transfer(resultPath);
        if (results.isEmpty()) {
            result.getSummaries().add(new JmeterReportSummary(TOTAL_LABEL));
            return;
        }
        long startTime = results.get(0).getStartTime();
        long endTime = getEndTime(startTime, results.get(results.size() - 1).getStartTime());

        JmeterResultParser parser = new JmeterResultParser(TOTAL_LABEL);
        parser.calcTPSAndRT(results, startTime, endTime);
        result.getSummaries().add(parser.getSummary());
        result.getRtMap().put(TOTAL_LABEL, parser.getRtList());
        result.getFrtMap().put(TOTAL_LABEL, parser.getFrtList());
        result.getTpsMap().put(TOTAL_LABEL, parser.getTpsList());
        Map<String, List<JmeterResult>> map = results.stream().collect(Collectors.groupingBy(JmeterResult::getLabel));
        for (Map.Entry<String, List<JmeterResult>> entry : map.entrySet()) {
            JmeterResultParser parserPer = new JmeterResultParser(entry.getKey());
            parserPer.calcTPSAndRT(results, startTime, endTime);
            result.getSummaries().add(parserPer.getSummary());
            result.getRtMap().put(entry.getKey(), parserPer.getRtList());
            result.getFrtMap().put(entry.getKey(), parserPer.getFrtList());
            result.getTpsMap().put(entry.getKey(), parserPer.getTpsList());
        }
    }

    private void calcTPSAndRT(List<JmeterResult> results, long startTime, long endTime) {
        if (Objects.isNull(results) || results.isEmpty()) {
            return;
        }

        int samplePer = 0;
        int latencyTotalPerSec = 0;
        int latencyFailPerSec = 0;
        long latencyTotal = 0;

        long start = startTime;
        long end = startTime + JFRConstants.MS_1000;

        boolean exist = false;
        for (int i = 0; start < endTime; start += JFRConstants.MS_1000, end += JFRConstants.MS_1000) {

            while (i < results.size() && results.get(i).getStartTime() >= start && results.get(i).getStartTime() < end) {
                summary.samplesIncrease();
                latencyTotalPerSec += results.get(i).getLatency();
                latencyTotal += results.get(i).getLatency();
                samplePer++;
                if (results.get(i).getResponseCode() >= MIN_ERROR_CODE) {
                    summary.failSamplesIncrease();
                    latencyFailPerSec += results.get(i).getLatency();
                }
                i++;
                exist = true;
            }
            if (exist) {
                rtList.add(new JmeterRT(start, latencyTotalPerSec / (double) samplePer));
                frtList.add(new JmeterRT(start, latencyFailPerSec / (double) samplePer));
                tpsList.add(new JmeterTPS(start, samplePer));
            } else {
                rtList.add(new JmeterRT(start, null));
                frtList.add(new JmeterRT(start, null));
                tpsList.add(new JmeterTPS(start, 0));
            }
            // re init
            latencyTotalPerSec = 0;
            samplePer = 0;
            latencyFailPerSec = 0;
            exist = false;
        }
        summary.setThroughput(summary.getSamples() * JFRConstants.MS_TO_S / (endTime - startTime));
        summary.setAverageLatency(latencyTotal / (double) summary.getSamples());
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
        int position50 = jmeterResults.size() * PERCENT_50 / 100 - 1;
        summary.setMedian(jmeterResults.get(position50).getLatency());
        int position90 = jmeterResults.size() * PERCENT_90 / 100 - 1;
        summary.setLatency90(jmeterResults.get(position90).getLatency());
        int position95 = jmeterResults.size() * PERCENT_95 / 100 - 1;
        summary.setLatency95(jmeterResults.get(position95).getLatency());
        int position99 = jmeterResults.size() * PERCENT_99 / 100 - 1;
        summary.setLatency99(jmeterResults.get(position99).getLatency());
    }

    private static long getEndTime(long startTime, long lastTime) {
        long duration = (lastTime - startTime) / JFRConstants.MS_1000 + 1;
        return duration * JFRConstants.MS_1000 + startTime;
    }
}
