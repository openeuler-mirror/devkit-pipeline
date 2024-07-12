package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.perload.DataBasePreLoad;
import com.huawei.devkit.code.inspector.utils.PropertiesUtils;
import com.huawei.devkit.code.inspector.wrappers.CheckStyleWrapper;

import java.io.IOException;
import java.util.Properties;

public class CodeInspector {

    public static void main(String[] args) {
        try {
            Properties properties = PropertiesUtils.loadProperties("config.properties");
            PropertiesUtils.configAndUpdate(properties);
            DataBasePreLoad.preload(properties);
            CheckStyleWrapper.main(args);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
