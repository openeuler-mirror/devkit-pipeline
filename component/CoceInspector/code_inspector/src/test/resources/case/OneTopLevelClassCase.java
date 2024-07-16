/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class OneTopLevelClassCase { // OK, first top-level class
    // methods
}

class Foo2 { // violation, second top-level class
    // methods
}

record Foo3 { // violation, third top-level "class"
    // methods
}