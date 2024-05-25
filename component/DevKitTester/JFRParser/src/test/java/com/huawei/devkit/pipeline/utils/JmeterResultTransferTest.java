package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.JmeterResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class JmeterResultTransferTest {

    /**
     * @utbot.classUnderTest {@link JmeterResultTransfer}
     * @utbot.methodUnderTest {@link JmeterResultTransfer#transfer(String)}
     * @utbot.throwsException {@link java.nio.file.InvalidPathException}
     */
    @Test
    @DisplayName("transfer:  -> ThrowInvalidPathException")
    public void testTransferThrowInvalidPathException() {
        String resultPath = "\u0000";
        Assertions.assertThrows(InvalidPathException.class, () -> JmeterResultTransfer.transfer(resultPath));
    }

    /**
     * @utbot.classUnderTest {@link JmeterResultTransfer}
     * @utbot.methodUnderTest {@link JmeterResultTransfer#transfer(String)}
     * @utbot.invokes com.huawei.devkit.pipeline.utils.JmeterResultTransfer#transferInner(java.lang.String)
     * @utbot.triggersRecursion of {@code transferInner}
     * @utbot.throwsException {@link NullPointerException} in: return new JmeterResultTransfer().transferInner(resultPath);
     */
    @Test
    @DisplayName("transfer: ThrowNullPointerException")
    public void testTransferThrowNullPointerException() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> JmeterResultTransfer.transfer(null));
    }

    @Test
    @DisplayName("transfer: Normal")
    public void testTransfer() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("result.csv");
        assert resource != null;
        Path resultPath = Paths.get(resource.toURI());
        List<JmeterResult> transferred = JmeterResultTransfer.transfer(resultPath.toString());
        Assertions.assertEquals(transferred.size(), 1000);
    }

}