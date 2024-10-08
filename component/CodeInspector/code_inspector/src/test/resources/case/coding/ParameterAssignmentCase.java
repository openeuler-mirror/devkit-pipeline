/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class ParameterAssignmentCase {
    int methodOne(int parameter) {
        if (parameter <= 0 ) {
            throw new IllegalArgumentException("A positive value is expected");
        }
        parameter -= 2;  // violation
        return parameter;
    }

    int methodTwo(int parameter) {
        if (parameter <= 0 ) {
            throw new IllegalArgumentException("A positive value is expected");
        }
        int local = parameter;
        local -= 2;  // OK
        return local;
    }

    IntPredicate obj = a -> ++a == 12; // violation
    IntBinaryOperator obj2 = (int a, int b) -> {
        a++;     // violation
        b += 12; // violation
        return a + b;
    };
    IntPredicate obj3 = a -> {
        int b = a; // ok
        return ++b == 12;
    };
}
