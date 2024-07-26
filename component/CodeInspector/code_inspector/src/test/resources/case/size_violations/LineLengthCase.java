public class LineLengthCase {
    void MyMethod() {
        final int VAR1 = 5; // violation
        final int var1 = 10;
        if(true){
            var1 = 10;
            var1 = 10;
        }

        var1 = 10;// violation ===========================================================================================

    }
}