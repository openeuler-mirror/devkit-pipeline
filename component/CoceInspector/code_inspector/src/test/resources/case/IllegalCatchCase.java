public  class IllegalCatchCase {
    public void test(){
        try {
            // some code here
        } catch (Exception e) { // violation

        }

        try {
            // some code here
        } catch (ArithmeticException e) { // OK

        } catch (RuntimeException e) { // violation, catching Exception is illegal
            and order of catch blocks doesn't matter
        }

        try {
            // some code here
        } catch (NullPointerException) { // violation, catching Exception is illegal

        }

        try {
            // some code here
        } catch (ArithmeticException e) { // OK

        }
    }
}