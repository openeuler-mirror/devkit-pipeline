/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class IllegalInstantiationCase {
    public class Boolean {
        boolean a;

        public Boolean (boolean a) { this.a = a; }
    }

    public void myTest (boolean a, int b) {
        Boolean c = new Boolean(a); // OK
        java.lang.Boolean d = new java.lang.Boolean(a); // violation, instantiation of
        // java.lang.Boolean should be avoided

        Integer e = new Integer(b); // violation, instantiation of
        // java.lang.Integer should be avoided
        Integer f = Integer.valueOf(b); // OK
    }
}