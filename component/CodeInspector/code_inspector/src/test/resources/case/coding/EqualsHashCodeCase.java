/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class EqualsHashCodeCase {
    public int hashCode() { // violation, no valid 'equals'
        return 0;
    }
    public boolean equals(String o) { return false; }
}

class ExampleNoHashCode {
    public boolean equals(Object o) { // violation, no 'hashCode'
        return false;
    }
    public boolean equals(String o) { return false; }
}

class ExampleBothMethods1 {
    public int hashCode() { return 0; }
    public boolean equals(Object o) { // ok, both methods exist
        return false;
    }
    public boolean equals(String o) { return false; }
}

class ExampleBothMethods2 {
    public int hashCode() { return 0; }
    public boolean equals(java.lang.Object o) { // ok, both methods exist
        return false;
    }
}

class ExampleNoValidHashCode {
    public static int hashCode(int i) { return 0; }
    public boolean equals(Object o) { // violation, no valid 'hashCode'
        return false;
    }
}

class ExampleNoValidEquals {
    public int hashCode() { // violation, no valid 'equals'
        return 0;
    }
    public static boolean equals(Object o, Object o2) { return false; }
}