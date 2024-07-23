public class InterfaceTypeParameterNameCase {

    interface FirstInterface<T> {
    }

    interface SecondInterface<t> { //violation
    }

    interface ThirdInterface<type> { // violation
    }

    interface ThirdInterface<InterfaceTypeParameterNameCase> {
    }

}