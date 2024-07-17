public class LocalVariableNameCase {
    void MyMethod() {
        for (int var = 1; var < 10; var++) {}
        for (int VAR = 1; VAR < 10; VAR++) {} // violation
        for (int i = 1; i < 10; i++) {}
        for (int var_1 = 0; var_1 < 10; var_1++) {} // violation
    }
}