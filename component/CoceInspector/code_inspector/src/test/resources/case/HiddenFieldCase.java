/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

public class HiddenFieldCase {

    private String field;
    private String testField;

    public HiddenFieldCase(String testField) { // OK, 'testField' param doesn't hide any field
    }
    public void method(String param) { // OK
        String field = param; // violation, 'field' variable hides 'field' field
    }
    public void setTestField(String testField) { // OK, 'testField' param doesn't hide any field
        this.field = field;
    }
    public HiddenFieldCase setField(String field) { // OK, 'field' param doesn't hide any field
        this.field = field;
    }
}