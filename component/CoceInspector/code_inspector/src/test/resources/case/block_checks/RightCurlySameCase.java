
public class  RightCurlySameCase{

    public void test() {
        boolean foo = false;
        try (Foo foo = new Foo(); Bar bar = new Bar()) {
            bar();
        }
        if (foo) {
            bar();
        } // violation, 'should be on the same line'
        // as the next part of a multi-block statement (one that directly
        // contains multiple blocks: if/else-if/else, do/while or try/catch/finally).
        else {
            bar();
        }

        if (foo) {
            bar();
        } else {
            bar();
        }

        if (foo) { bar(); } int i = 0;
        // violation above, 'should be alone on a line.'

        if (foo) { bar(); }
        i = 0;

        try {
            bar();
        } // violation, 'should be on the same line'
        // as the next part of a multi-block statement (one that directly
        // contains multiple blocks: if/else-if/else, do/while or try/catch/finally).
        catch (Exception e) {
            bar();
        }

        try {
            bar();
        } catch (Exception e) {
            bar();
        } finally {
            bar(); }

    }

    private void bar() {
    }

    public void testSingleLine() { bar(); } // OK, because singleline is allowed
}