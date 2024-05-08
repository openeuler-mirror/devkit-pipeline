package com.huawei.devkit.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devkit.pipeline.bo.CommandLineParams;
import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import com.huawei.devkit.pipeline.parser.JFRParser;
import com.huawei.devkit.pipeline.parser.JmeterResultParser;
import com.huawei.devkit.pipeline.parser.ParamsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            CommandLineParams params = ParamsParser.parse(args);
            params.fillMaps();

            PerformanceTestResult result = new PerformanceTestResult();
            // jmeter结果解析
            logger.info("start to parse jmeter result !!!");
            JmeterResultParser jmeterResultParser = new JmeterResultParser();
//            jmeterResultParser.parse(params.getJmeterResult());
            logger.info("finish to parse jmeter result !!!");

            // jfr解析
            logger.info("start to parse jfr !!!");
            List<LatencyTopInfo> latencyKes = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.getJfrPathMap().entrySet()) {
                String gap = params.getNodesTimeGapMap().get(entry.getKey());
                int timeGap = gap == null ? 0 : Integer.parseInt(gap);
                JFRParser.parse(entry.getValue(), latencyKes, timeGap, result);
            }
            result.toStandardFlames();
            logger.info("finish to parse jfr !!!");

            //数据持久化
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("result.json"), result);
            logger.info("the end");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}