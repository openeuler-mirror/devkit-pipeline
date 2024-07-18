/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class MissingJavadocMethodCase {
    private String text;

    public void test() {} // violation, method is missing javadoc
    public String getText() { return text; } // OK
    public void setText(String text) { this.text = text; } // OK
    private void test1() {} // OK
    void test2() {} // OK
    protected void test3() {} // violation, method is missing javadoc
}