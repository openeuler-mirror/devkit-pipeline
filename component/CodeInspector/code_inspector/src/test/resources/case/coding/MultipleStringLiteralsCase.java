/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class MultipleStringLiterals {
    String a = "StringContents";
    String a1 = "unchecked";
    @SuppressWarnings("unchecked") // OK, duplicate strings are ignored in annotations
    public void myTest() {
        String a2 = "StringContents"; // OK, two occurrences are allowed
        String a3 = "DoubleString" + "DoubleString"; // OK, two occurrences are allowed
        String a4 = "SingleString"; // OK
        String a5 = ", " + ", " + ", " + ", "; // violation, four occurrences are NOT allowed
    }
}