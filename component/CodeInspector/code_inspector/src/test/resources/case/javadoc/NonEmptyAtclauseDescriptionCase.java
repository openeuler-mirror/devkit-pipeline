/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

class NonEmptyAtclauseDescriptionCase
{
    // Violation for param "b" and at tags "deprecated", "throws" and "return".
    /**
     * Some summary.
     *
     * <code>@param</code> a Some description
     * <code>@param</code> b
     * <code>@deprecated</code>
     * <code>@throws</code> Exception
     * <code>@return</code>
     */
    public int method(String a, int b) throws Exception {
        return 1;
    }

    // Violation for param "b" and at tags "deprecated", "throws" and "return".
    /**
     * Some summary.
     *
     * @param a Some description
     * @param b
     * @deprecated
     * @throws Exception
     * @return
     */
    public int method(String a, int b) throws Exception {
        return 1;
    }
}