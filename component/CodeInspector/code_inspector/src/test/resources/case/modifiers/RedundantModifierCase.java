public class RedundantModifierCase {

}

class PackagePrivateClass {
    public PackagePrivateClass() {} // violation expected
}

public enum EnumClass {
    FIELD_1,
    FIELD_2 {
        @Override
        public final void method1() {} // violation expected
    };

    public void method1() {}
    public final void method2() {} // no violation expected
}