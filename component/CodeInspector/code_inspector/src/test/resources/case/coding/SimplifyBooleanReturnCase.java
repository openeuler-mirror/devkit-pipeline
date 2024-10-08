/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class SimplifyBooleanReturnCase {

    private boolean cond;
    private Foo a;
    private Foo b;

    public boolean check1() {
        if (cond) { // violation, can be simplified
            return true;
        }
        else {
            return false;
        }
    }

    // Ok, simplified version of check1()
    public boolean check2() {
        return cond;
    }

    // violations, can be simplified
    public boolean check3() {
        if (cond == true) { // can be simplified to "if (cond)"
            return false;
        }
        else {
            return true; // can be simplified to "return !cond"
        }
    }

    // Ok, can be simplified but doesn't return a Boolean
    public Foo choose1() {
        if (cond) {
            return a;
        }
        else {
            return b;
        }
    }

    // Ok, simplified version of choose1()
    public Foo choose2() {
        return cond ? a: b;
    }

}