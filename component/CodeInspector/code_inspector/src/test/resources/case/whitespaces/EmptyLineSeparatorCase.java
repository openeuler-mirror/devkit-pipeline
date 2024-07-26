///////////////////////////////////////////////////
//HEADER
///////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle.checks.whitespace.emptylineseparator;
import java.io.Serializable;
// violation 2 lines above ''package' should be separated from previous line'
// violation 2 lines above ''import' should be separated from previous line'

class Example1 {

    int var1 = 1;
    int var2 = 2;


    int var3 = 3; // violation ''VARIABLE_DEF' should be separated from previous line'


    void method1() {} // violation ''METHOD_DEF' should be separated from previous line'
    void method2() { // violation ''METHOD_DEF' should be separated from previous line'
        int var4 = 4;

        // zhusi
        int var5 = 5; // violation ''METHOD_DEF' should be separated from previous line'


        int var6 = 5;
    }
}