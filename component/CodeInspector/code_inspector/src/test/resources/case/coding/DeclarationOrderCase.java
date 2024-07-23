public class DeclarationOrderCase {

    public int a;
    protected int b;
    public int c;            // violation, variable access definition in wrong order

    DeclarationOrderCase() {
        this.a = 0;
    }

    private void foo2() {
        // This method does nothing
    }
    public void foo() {
        // This method does nothing
    }

    DeclarationOrderCase(int a) {            // OK, validation of constructors ignored
        this.a = a;
    }

    private String name;     // violation, instance variable declaration in wrong order
}