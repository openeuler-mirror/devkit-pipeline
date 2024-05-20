package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.JmeterResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JmeterResultTransfer {
    private int timeStampIndex;

    private int latencyIndex;

    private int responseCodeIndex;

    private int labelIndex;

    public static List<JmeterResult> transfer(String resultPath) throws Exception {
        return new JmeterResultTransfer().transferInner(resultPath);
    }

    public static boolean isNum(String str) {
        if (str == null || str.isEmpty())
            return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private List<JmeterResult> transferInner(String resultPath) throws Exception {
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
                int responseCode = 700;
                // 当url完全错误时，不会有responseCode
                if (isNum(fields[responseCodeIndex])) {
                    responseCode = Integer.parseInt(fields[responseCodeIndex]);
                }
                results.add(new JmeterResult(Long.parseLong(fields[timeStampIndex]),
                        responseCode,
                        Integer.parseInt(fields[latencyIndex]),
                        fields[labelIndex]));
            }
        }
        return results;
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
            } else if ("label".equalsIgnoreCase(header[i])) {
                labelIndex = i;
            }
        }
    }
}
