public class ClassTypeParameterNameCase {
    class MyClass1<T> {}
    class MyClass2<t> {}        // violation

    class MyClass2<TypeNameT> {}
}