/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class EmptyStatementCase {
    public void foo() {
        int i = 5;
        if(i > 3); // violation
        i++;
        for (i = 0; i < 5; i++); // violation
        for (i = 0; i < 5; i++){

        } // no violation
        i++;
        while (i > 10)
            i++;
    }
}
