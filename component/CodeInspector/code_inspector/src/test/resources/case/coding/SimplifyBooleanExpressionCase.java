/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class SimplifyBooleanExpressionCase {

    public void bar() {
        boolean a, b;
        Foo c, d, e;

        if (!false) {};       // violation, can be simplified to true

        if (a == true) {};    // violation, can be simplified to a
        if (a == b) {};       // OK
        if (a == false) {};   // violation, can be simplified to !a
        if (!(a != true)) {}; // violation, can be simplified to a

        e = (a || b) ? c : d;     // OK
        e = (a || false) ? c : d; // violation, can be simplified to a
        e = (a && b) ? c : d;     // OK

        int s = 12;
        boolean m = s > 1 ? true : false; // violation, can be simplified to s > 1
        boolean f = c == null ? false : c.someMethod(); // OK
    }

}