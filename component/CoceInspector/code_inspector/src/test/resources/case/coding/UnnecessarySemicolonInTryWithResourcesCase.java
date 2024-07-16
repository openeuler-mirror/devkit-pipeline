/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class UnnecessarySemicolonInTryWithResourcesCase {
    void method() throws IOException {
        try(Reader r1 = new PipedReader();){} // violation
        try(Reader r4 = new PipedReader();Reader r5 = new PipedReader()
            ;){} // violation
        try(Reader r6 = new PipedReader();
            Reader r7
                    = new PipedReader();
        ){}
    }
}