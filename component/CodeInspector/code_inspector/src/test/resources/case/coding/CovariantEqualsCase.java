/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class CovariantEqualsCase {
    public boolean equals(CovariantEqualsCase i) {  // violation
        return false;
    }
}

class CovariantEqualsCase01 {
    public boolean equals(CovariantEqualsCase01 i) {  // no violation
        return false;
    }

    public boolean equals(Object i) {
        return false;
    }
}

class CovariantEqualsCase01 {

}