public class NestedTryDepthCase {
    public void test(){
        try {
            try {
                try {
                    try {
                        try { // violation, current depth is 4, max allowed depth is 3
                        } catch (Exception e) {
                        }
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
    }
}