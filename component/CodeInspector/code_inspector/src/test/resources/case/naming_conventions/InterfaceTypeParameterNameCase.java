public class InterfaceTypeParameterNameCase {

    interface FirstInterface<T> {
    }

    interface SecondInterface<t> {
    }

    interface ThirdInterface<type> {
    } // violation

    interface ThirdInterface<InterfaceTypeParameterNameCase> {
    } // violation

}