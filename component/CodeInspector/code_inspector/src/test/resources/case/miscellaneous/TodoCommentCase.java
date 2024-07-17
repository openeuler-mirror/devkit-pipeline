public class TodoCommentCase {
    int i;
    int x;
    public void test() {
        i++;   // TODO: do differently in future    // violation
        i++;   // todo: do differently in future
        i=i/x; // FIXME: handle x = 0 case
        i=i/x; // FIX :  handle x = 0 case
    }
}