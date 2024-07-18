/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class UnnecessarySemicolonAfterTypeMemberDeclarationCase {
    ; // violation, standalone semicolon
    {}; // violation, extra semicolon after init block
    static {}; // violation, extra semicolon after static init block
    UnnecessarySemicolonAfterTypeMemberDeclarationCase(){}; // violation, extra semicolon after constructor definition
    void method() {}; // violation, extra semicolon after method definition
    int field = 10;; // violation, extra semicolon after field declaration

    {
        ; // no violation, it is empty statement inside init block
    }

    static {
        ; // no violation, it is empty statement inside static init block
    }

    void anotherMethod() {
        ; // no violation, it is empty statement
        if(true); // no violation, it is empty statement
    }
}