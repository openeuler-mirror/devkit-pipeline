public class LocalFinalVariableNameCase {
    void MyMethod() {
        try {
            final int VAR1 = 5; // violation
            final int var1 = 10;
        } catch (Exception ex) {
            final int VAR2 = 15; // violation
            final int dataName = 20;
        }
    }
}