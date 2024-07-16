public class OverloadMethodsDeclarationOrderCase {
    public void foo(int i) {}
    // comments between overloaded methods are allowed.
    public void foo(String s) {}
    public void foo(String s, int i) {}
    public void notFoo() {} // violation. Have to be after foo(String s, int i)
    public void foo(int i, String s) {}
    private interface ExampleInterface() {}
}