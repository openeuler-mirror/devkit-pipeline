public class ConstructorsDeclarationGroupingCase {

    int x;

    ConstructorsDeclarationGroupingCase() {}

    ConstructorsDeclarationGroupingCase(String s){}

    void foo() {}

    ConstructorsDeclarationGroupingCase(int x) {} // violation

    ConstructorsDeclarationGroupingCase(String s, int x) {} // violation

    private enum ExampleEnum {

        ONE, TWO, THREE;

        ExampleEnum() {}

        ExampleEnum(int x) {}

        final int x = 10;

        ExampleEnum(String str) {} // violation

        void foo() {}
    }

    ConstructorsDeclarationGroupingCase(float f) {} // violation
}
