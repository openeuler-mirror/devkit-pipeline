package com.huawei.devkit.pipeline.parse;

import com.huawei.devkit.pipeline.bo.*;
import jdk.jfr.consumer.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JFRParser {
    public static Long ALL = -1L;

    public static void parse(String filePath, List<LatencyTopInfo> top10, PerformanceTestResult result) throws Exception {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new Exception("the file not exist");
        }

        List<MemInfo> memInfos = new ArrayList<>();
        List<CpuInfo> cpuInfos = new ArrayList<>();
        String fileName = path.getFileName().toString();
        FlameItem flame = new FlameItem(fileName, 0);
        try (RecordingFile file = new RecordingFile(path)) {
            while (file.hasMoreEvents()) {
                RecordedEvent event = file.readEvent();
                long startTime = event.getStartTime().toEpochMilli();
                if (event.getEventType().getName().equals("jdk.GCHeapSummary")) {
                    RecordedObject headSpace = event.getValue("heapSpace");
                    long committedSize = headSpace.getLong("committedSize");
                    long reservedSize = headSpace.getLong("reservedSize");
                    long heapUsed = event.getLong("heapUsed");
                    memInfos.add(new MemInfo(startTime, committedSize, reservedSize, heapUsed));
                } else if (event.getEventType().getName().equals("jdk.ExecutionSample")) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    flame.addFlameItemByRecordedFrame(stackTrace.getFrames());
                    addDurationFlame(startTime, stackTrace.getFrames(), top10);
                } else if (event.getEventType().getName().equals("jdk.NativeMethodSample")) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    flame.addFlameItemByRecordedFrame(stackTrace.getFrames());
                    addDurationFlame(startTime, stackTrace.getFrames(), top10);
                } else if (event.getEventType().getName().equals("jdk.CPULoad")) {
                    float jvmSystem = event.getFloat("jvmSystem");
                    float jvmUser = event.getFloat("jvmUser");
                    float machineTotal = event.getFloat("machineTotal");
                    cpuInfos.add(new CpuInfo(startTime, jvmSystem, jvmUser, machineTotal));
                }
            }
        }
        result.getCpu().put(fileName, cpuInfos);
        result.getMemory().put(fileName, memInfos);
        result.getFlame().get(ALL).addSubFlameItem(flame);
        for (LatencyTopInfo latencyTop : top10) {
            result.getFlame().put(latencyTop.getKey(), latencyTop.getFlame());
        }
    }

    private static void addDurationFlame(long startTime, List<RecordedFrame> frames, List<LatencyTopInfo> top10) {
        for (LatencyTopInfo latencyTop : top10) {
            if (startTime > latencyTop.getStartTime() && startTime < latencyTop.getEndTime()) {
                latencyTop.getFlame().addFlameItemByRecordedFrame(frames);
            }
        }
    }
}
