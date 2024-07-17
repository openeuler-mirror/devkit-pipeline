public class MethodTypeParameterNameCase {
    public <T> void method1() {}
    public <a> void method2() {} // violation
    public <K, V> void method3() {}
    public <k, V> void method4() {} // violation
}