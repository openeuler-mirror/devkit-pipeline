package com.huawei.devkit.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devkit.pipeline.bo.CommandLineParams;
import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import com.huawei.devkit.pipeline.parse.JFRParser;
import com.huawei.devkit.pipeline.parse.ParamsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        int a = 5 / 6;
        System.out.println(a);
        try {
            CommandLineParams params = ParamsParser.parse(args);
            System.out.println(params.getJfrPaths());
            logger.info("start to parse jfr !!!");
            List<LatencyTopInfo> latencyKes = new ArrayList<>();
            PerformanceTestResult result = new PerformanceTestResult();
            for (String jfrPath : params.getJfrPaths()) {
                JFRParser.parse(jfrPath, latencyKes, result);
            }
            result.toStandardFlames();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("result.json"), result);
            logger.info("success parse jfr");
        } catch (Exception ex) {
            logger.error(ex);
        }

    }
}