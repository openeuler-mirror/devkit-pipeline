/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

/** This is valid single-line Javadoc. */
class JavadocMissingWhitespaceAfterAsteriskCase {
    /**
     *This is invalid Javadoc.
     */
    int invalidJavaDoc;
    /**
     * This is valid Javadoc.
     */
    void validJavaDocMethod() {
    }
    /**This is invalid single-line Javadoc. */
    void invalidSingleLineJavaDocMethod() {
    }
    /** This is valid single-line Javadoc. */
    void validSingleLineJavaDocMethod() {
    }
}