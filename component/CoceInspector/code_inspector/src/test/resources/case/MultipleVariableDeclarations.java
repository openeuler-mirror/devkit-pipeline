/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class MultipleVariableDeclarations {
    public void myTest() {
        int mid;
        int high;
        // ...

        int lower, higher; // violation
        // ...

        int value,
                index; // violation
        // ...

        int place = mid, number = high;  // violation
    }
}