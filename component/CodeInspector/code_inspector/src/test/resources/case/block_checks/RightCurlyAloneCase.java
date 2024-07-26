/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */


public class RightCurlyAloneCase {

    public void test() {

        boolean foo = false;
        if (foo) {
            bar();
        }
        if (foo) {
            bar();
        } else {
            bar(); }
        // violation above, 'should be alone on a line.'
        if (foo) {
            bar();
        } else {
            bar();
        } bar();
        // violation above, 'should be alone on a line.'

        if (foo) {
            bar();
        } else {
            bar();
        }

        try {
            bar();
        } catch (Exception e) { bar();        }
            // OK above because config is set to token METHOD_DEF and LITERAL_ELSE


        finally {

        } // violation

    }

    private void bar() {
    }

    public void violate() { Object bar = "bar"; }
    // violation above, 'should be alone on a line.'

    public void ok() {
        bar();
    }
}

