package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.CpuInfo;
import com.huawei.devkit.pipeline.bo.FlameItem;
import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import com.huawei.devkit.pipeline.bo.MemInfo;
import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JFRParser {
    public static Long ALL = -1L;

    private static final String GC_HEAP_SUMMARY_EVENT = "jdk.GCHeapSummary";
    private static final String EXECUTION_SAMPLE_EVENT = "jdk.ExecutionSample";
    private static final String NATIVE_METHOD_SAMPLE_EVENT = "jdk.NativeMethodSample";
    private static final String CPU_LOAD_EVENT = "jdk.CPULoad";

    public static void parse(String filePath, List<LatencyTopInfo> top10,
                             int timeGap, PerformanceTestResult result, String nodeIP) throws Exception {
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
                long startTime = event.getStartTime().toEpochMilli() + timeGap;
                if (event.getEventType().getName().equals(GC_HEAP_SUMMARY_EVENT)) {
                    RecordedObject headSpace = event.getValue("heapSpace");
                    long committedSize = headSpace.getLong("committedSize");
                    long reservedSize = headSpace.getLong("reservedSize");
                    long heapUsed = event.getLong("heapUsed");
                    memInfos.add(new MemInfo(startTime, committedSize, reservedSize, heapUsed));
                } else if (event.getEventType().getName().equals(EXECUTION_SAMPLE_EVENT)) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    flame.addFlameItemByRecordedFrame(stackTrace.getFrames());
                    addDurationFlame(startTime, stackTrace.getFrames(), top10, fileName);
                } else if (event.getEventType().getName().equals(NATIVE_METHOD_SAMPLE_EVENT)) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    flame.addFlameItemByRecordedFrame(stackTrace.getFrames());
                    addDurationFlame(startTime, stackTrace.getFrames(), top10, fileName);
                } else if (event.getEventType().getName().equals(CPU_LOAD_EVENT)) {
                    float jvmSystem = event.getFloat("jvmSystem");
                    float jvmUser = event.getFloat("jvmUser");
                    float machineTotal = event.getFloat("machineTotal");
                    cpuInfos.add(new CpuInfo(startTime, jvmSystem, jvmUser, machineTotal));
                }
            }
        }
        Map<String, List<CpuInfo>> cpuMap = result.getCpuMap().get(nodeIP);
        cpuMap.put(fileName, cpuInfos);
        Map<String, List<MemInfo>> memoryMap = result.getMemoryMap().get(nodeIP);
        memoryMap.put(fileName, memInfos);
        result.getFlame().get(ALL).addSubFlameItem(flame);
        for (LatencyTopInfo latencyTop : top10) {
            result.getFlame().put(latencyTop.getKey(), latencyTop.getFlame());
        }
    }

    private static void addDurationFlame(long startTime, List<RecordedFrame> frames, List<LatencyTopInfo> top10, String filename) {
        for (LatencyTopInfo latencyTop : top10) {
            if (startTime > latencyTop.getStartTime() && startTime < latencyTop.getEndTime()) {
                latencyTop.getFlame().addFlameItemByRecordedFrame(frames, filename);
            }
        }
    }
}
