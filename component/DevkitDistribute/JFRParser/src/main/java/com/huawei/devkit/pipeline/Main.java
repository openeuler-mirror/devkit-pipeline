package com.huawei.devkit.pipeline;

import com.huawei.devkit.pipeline.bo.FlameItem;
import jdk.jfr.consumer.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("start to parse jfr !!!");
        Path filePath = Paths.get(args[0]);
        if (!Files.exists(filePath)) {
            throw new Exception("the file not exist");
        }
        FlameItem flame = new FlameItem("all", 0);
        try (RecordingFile file = new RecordingFile(filePath)) {
            while (file.hasMoreEvents()) {
                RecordedEvent event = file.readEvent();
                long startTime = event.getStartTime().toEpochMilli();
                if (event.getEventType().getName().equals("jdk.GCHeapSummary")) {
                    RecordedObject headSpace = event.getValue("heapSpace");
                    long committedSize = headSpace.getLong("committedSize");
                    long reservedSize = headSpace.getLong("reservedSize");
                    long useSize = event.getLong("heapUsed");
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
                }
            }
        }
        logger.info("success parse jfr");
    }

    public static void addStackTrace(RecordedStackTrace trace) {
        if (trace == null) {
            return;
        }
        List<RecordedFrame> recordedFrames = trace.getFrames();
    }

    public static boolean checkIsFile(Path filePath) {
        return Files.exists(filePath);
    }
}