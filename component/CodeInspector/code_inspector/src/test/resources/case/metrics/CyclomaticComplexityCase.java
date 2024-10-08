public class CyclomaticComplexityCase
{
    // Cyclomatic Complexity = 11
    int a, b, c, d, n;
    public void foo() { // 1, function declaration
        if (a == 1) { // 2, if
            fun1();
        } else if (a == b // 3, if
                && a == c) { // 4, && operator
            if (c == 2) { // 5, if
                fun2();
            }
        } else if (a == d) { // 6, if
            try {
                fun4();
            } catch (Exception e) { // 7, catch
            }
        } else {
            switch(n) {
                case 1: // 8, case
                    fun1();
                    break;
                case 2: // 9, case
                    fun2();
                    break;
                case 3: // 10, case
                    fun3();
                    break;
                default:
                    break;
            }
        }
        d = a < 0 ? -1 : 1; // 11, ternary operator
    }
}