public class MissingSwitchDefaultCase {
    public void test(int i, String o){
        switch (i) {
            case 1:
                break;
            case 2:
                break;
            default: // OK
                break;
        }
        switch (o) {
            case String s: // type pattern
                System.out.println(s);
                break;
            case Integer i: // type pattern
                System.out.println("Integer");
                break;
            default:    // will not compile without default label, thanks to type pattern label usage
                break;
        }
    }
}