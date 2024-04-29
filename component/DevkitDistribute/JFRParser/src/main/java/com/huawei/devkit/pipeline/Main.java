package com.huawei.devkit.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devkit.pipeline.bo.*;
import com.huawei.devkit.pipeline.parse.ParamsParser;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        try {
            CommandLineParams params = ParamsParser.parse(args);
            logger.info("start to parse jfr !!!");
        } catch (Exception ex) {
            logger.error(ex);
            return;
        }

        System.out.println(params.getJfrPaths());
        Path filePath = Paths.get(args[0]);
        if (!Files.exists(filePath)) {
            throw new Exception("the file not exist");
        }
        PerformanceTestResult result = new PerformanceTestResult();
        FlameItem flame = new FlameItem("all", 0);
        try (RecordingFile file = new RecordingFile(filePath)) {
            while (file.hasMoreEvents()) {
                RecordedEvent event = file.readEvent();
                long startTime = event.getStartTime().toEpochMilli();
                if (event.getEventType().getName().equals("jdk.GCHeapSummary")) {
                    RecordedObject headSpace = event.getValue("heapSpace");
                    long committedSize = headSpace.getLong("committedSize");
                    long reservedSize = headSpace.getLong("reservedSize");
                    long heapUsed = event.getLong("heapUsed");
                    result.getMemory().add(new MemInfo(startTime, committedSize, reservedSize, heapUsed));
                } else if (event.getEventType().getName().equals("jdk.ExecutionSample")) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    flame.addFlameItem(stackTrace.getFrames());
                } else if (event.getEventType().getName().equals("jdk.NativeMethodSample")) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    flame.addFlameItem(stackTrace.getFrames());
                } else if (event.getEventType().getName().equals("jdk.CPULoad")) {
                    float jvmSystem = event.getFloat("jvmSystem");
                    float jvmUser = event.getFloat("jvmUser");
                    float machineTotal = event.getFloat("machineTotal");
                    result.getCpu().add(new CpuInfo(startTime, jvmSystem, jvmUser, machineTotal));
                }
            }
        }
        flame.toStandardFlame();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("names.json"), flame);
        System.out.println("xxxxxx");
        logger.info("success parse jfr");
    }
}