/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class UnnecessarySemicolonAfterOuterTypeDeclarationCase {
    class Nested {

    }; // OK, nested type declarations are ignored

}; // violation

interface B {

}; // violation

enum C {

}; // violation

@interface D {

}; // violation