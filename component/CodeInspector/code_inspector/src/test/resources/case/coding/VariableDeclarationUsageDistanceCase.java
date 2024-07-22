public class VariableDeclarationUsageDistanceCase {

    public void foo1() {
        int num;        // violation, distance = 4
        final double PI;   // OK, final variables not checked
        System.out.println("Statement 1");
        System.out.println("Statement 2");
        System.out.println("Statement 3");
        num = 1;
        PI = 3.14;
    }

    public void foo2() {
        int a;          // OK, used in different scope
        int b;          // OK, used in different scope
        int count = 0;  // OK, used in different scope

        {
            System.out.println("Inside inner scope");
            a = 1;
            b = 2;
            count++;
        }
    }
}