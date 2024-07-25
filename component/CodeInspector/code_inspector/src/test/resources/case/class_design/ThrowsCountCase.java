/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class Test {
    public void myFunction() throws CloneNotSupportedException,
            ArrayIndexOutOfBoundsException,
            StringIndexOutOfBoundsException,
            IllegalStateException,
            NullPointerException { // ok, max allowed is 4
        // body
    }

    public void myFunc() throws ArithmeticException,
            NumberFormatException { // ok
        // body
    }

    private void privateFunc() throws CloneNotSupportedException,NullPointerException,
            ClassNotFoundException,
            IllegalAccessException,
            ArithmeticException,
            ClassCastException { // violation,
        // body
    }

}