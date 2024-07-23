public class CommentsIndentationCase {
    boolean bool = true;
    double d = 3.14;

    public void foo43() {
        try {
            int a;
            if (a > 0) {
                System.out.println("a > 0"); // sjdkfks
                    // a < 0
            } else if (a < 0) {
                System.out.println("a < 0");
            }
            // 为什么我们要在这里捕获异常?
        } catch (IOException e) {

        }
    }
}