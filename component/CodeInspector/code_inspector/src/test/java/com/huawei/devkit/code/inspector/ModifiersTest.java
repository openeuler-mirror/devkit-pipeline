package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.api.Test;

public class ModifiersTest {
    @Test
    void testModifierOrder() {
        TestUtil.execute("modifiers", "ModifierOrder");
    }
}
