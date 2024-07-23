import java.io.
        IOException; // OK, dot is on the previous line

class SeparatorWrapCase {
    String s;

    public void foo(int a,
                    int b) { // OK, comma is on the previous line
    }

    public void bar(int p
            , int q) { // violation '',' should be on the previous line'
        if (s
                .isEmpty()) { // violation ''.' should be on the previous line'
        }
    }
}