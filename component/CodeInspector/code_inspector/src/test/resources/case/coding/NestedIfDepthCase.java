public class NestedIfDepthCase {

    public void test(){
        if (true) {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) { // violation, nested if-else depth is 4 (max allowed is 3)
                            if (true) {} // violation, nested if-else depth is 5 (max allowed is 3)
                            else {}
                        }
                    }
                }
            }
        }
    }
}