public class EmptyCatchBlockCase {
    private void exampleMethod1() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException expected) {
        } // violation above
    }

    private void exampleMethod2() {
        try {
            throw new RuntimeException();
        } catch (Exception ignore) {
            // no handling
        } // ok, catch block has comment
    }

    private void exampleMethod3 () {
        try {
            throw new RuntimeException();
        } catch (RuntimeException o) {
        } // violation above
    }

    private void exampleMethod4 () {
        try {
            throw new RuntimeException();
        } catch (RuntimeException ex) {
            // This is expected
        }
    }
}
