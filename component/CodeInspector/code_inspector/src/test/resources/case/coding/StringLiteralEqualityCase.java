public class StringLiteralEqualityCase {
    public void test(){
        String status = "pending";

        if (status == "done") {} // violation

        while (status != "done") {} // violation

        boolean flag = (status == "done"); // violation

        boolean flag = (status.equals("done")); // OK

        String name = "X";
        if (name == getName()) {}
        // OK, limitation that check cannot tell runtime type returned from method call
    }
}