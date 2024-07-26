class LambdaParameterNameCase {
    Function<String, String> function1 = s -> s.toLowerCase();
    Function<String, String> function2 =
            S -> S.toLowerCase(); // violation
}
