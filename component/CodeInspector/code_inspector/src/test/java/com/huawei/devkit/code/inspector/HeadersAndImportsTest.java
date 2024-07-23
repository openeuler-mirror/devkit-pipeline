package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HeadersAndImportsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "RegexpHeader", "AvoidStarImport", "CustomImportOrder",
        "UnusedImports", "RedundantImport"
    })
    void testHeadersAndImports(String rule) {
        TestUtil.execute("headers_and_imports", rule);
    }
}
