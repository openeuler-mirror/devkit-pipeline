/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class JavadocMethodCase {

    /**
     *
     */
    Test(int x) {             // violation, param tag missing for x
    }

    /**
     *
     */
    public int foo(int p1) {  // violation, param tag missing for p1
        return p1;            // violation, return tag missing
    }

    /**
     *
     * @param p1 The first number
     */
    @Deprecated
    private int boo(int p1) {
        return p1;            // ok, only public methods checked
    }

    /**
     *
     */
    void bar(int p1) {        // violation, param tag missing for p1
    }                         // ok, no return tag for void method
}