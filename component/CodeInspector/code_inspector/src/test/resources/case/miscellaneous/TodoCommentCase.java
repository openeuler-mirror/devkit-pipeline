public class TodoCommentCase {
    int i;
    int x;
    public void test() {
        i++;   // TODO: do differently in future    // violation
        i++;   // todo: do differently in future // violation
        i=i/x; // FIXME: handle x = 0 case // violation
        i=i/x; // FIX :  handle x = 0 case
    }
}