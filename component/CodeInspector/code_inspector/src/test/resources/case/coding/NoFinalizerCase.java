/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class NoFinalizerCase {

    protected void finalize() throws Throwable { // violation
        try {
            System.out.println("overriding finalize()");
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }
}
