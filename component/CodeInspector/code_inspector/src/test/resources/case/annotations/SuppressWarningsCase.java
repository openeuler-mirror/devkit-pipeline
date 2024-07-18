// ok below, since we are only checking for '^unchecked$|^unused$'
@SuppressWarnings("")
class SuppressWarningsCase {
    // ok below as VARIABLE_DEF is not configured in tokens to check
    @SuppressWarnings("")
    final int num1 = 1;
    @SuppressWarnings("all")
    final int num2 = 2;
    @SuppressWarnings("unused")
    final int num3 = 3;

    // ok below as PARAMETER_DEF is not configured in tokens to check
    void foo1(@SuppressWarnings("unused") int param) {}

    // ok below, since we are only checking for '^unchecked$|^unused$'
    @SuppressWarnings("all")
    void foo2(int param) {}

    // violation below, 'The warning 'unused' cannot be suppressed at this location'
    @SuppressWarnings(true?"all":"unchecked")
    void foo4(int param) {}
}

// violation below, 'The warning 'unchecked' cannot be suppressed at this location'
@SuppressWarnings("unchecked")
class Test2 {}