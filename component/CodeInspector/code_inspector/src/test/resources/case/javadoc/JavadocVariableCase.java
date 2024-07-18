/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

import java.util.logging.Logger;

public class JavadocVariableCase {
    private int a;

    /**
     * Some description here
     */
    private int b;
    public Logger logger;
    protected int c;// violation
    public int d; // violation
    /*package*/ int e;
}