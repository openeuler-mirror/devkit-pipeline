/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class JavadocContentLocationCase {

    // violation below 'Javadoc content should start from the next line.'
    /** This comment causes a violation because it starts from the first line
     * and spans several lines.
     */
    private int field1;

    /**
     * This comment is OK because it starts from the second line.
     */
    private int field12;

    /** This comment is OK because it is on the single-line. */
    private int field3;

    private int field4;
}
