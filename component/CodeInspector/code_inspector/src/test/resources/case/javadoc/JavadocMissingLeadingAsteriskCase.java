/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

/**
 * Valid Java-style comment.
 *
 * <pre>
 *   int value = 0;
 * </pre>
 */
class JavadocMissingLeadingAsteriskCase {}

/** Valid Scala-style comment.
 * Some description here.
 **/
class ScalaStyle {}

/** **
 * Asterisks on first and last lines are optional.
 * */
class Asterisks {}

/** No asterisks are required for single-line comments. */
class SingleLine {}

/**

 */ // violation above, 'Javadoc line should start with leading asterisk'
class BlankLine {}

/** Wrapped
 single-line comment */
// violation above, 'Javadoc line should start with leading asterisk'
class Wrapped {}

/**
 * <pre>
 int value; // violation, 'Javadoc line should start with leading asterisk'
 * </pre>
 */
class Example1 {}