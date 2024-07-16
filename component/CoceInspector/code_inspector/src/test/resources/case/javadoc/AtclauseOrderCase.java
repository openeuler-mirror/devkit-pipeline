/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

/**
 * Some javadoc.
 *
 * @author Some javadoc.
 * @version Some javadoc.
 * @param Some javadoc.
 * @return Some javadoc.
 * @throws Some javadoc.
 * @exception Some javadoc.
 * @see Some javadoc.
 * @since Some javadoc.
 * @serial Some javadoc.
 * @serialField
 * @serialData
 */
public class Example1 {}

class Valid1 implements Serializable {}

/**
 * Some javadoc.
 *
 * @since Some javadoc.
 * @version Some javadoc. // violation
 * @deprecated
 * @see Some javadoc. // violation
 */
class Invalid1 implements Serializable {}

/**
 * Some javadoc.
 *
 * @since Some javadoc.
 * @version Some javadoc. // violation
 * @deprecated
 * @see Some javadoc. // violation
 * @author max // violation
 */
enum Test1 {}