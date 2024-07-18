/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class SuperFinalizeCase {
    protected void finalize() throws Throwable {
        System.out.println("In finalize block");
        super.finalize(); // OK, calls super.finalize()
    }
}
public class SuperFinalizeCase01 {
    protected void finalize() throws Throwable { // violation
        System.out.println("In finalize block");
    }
}