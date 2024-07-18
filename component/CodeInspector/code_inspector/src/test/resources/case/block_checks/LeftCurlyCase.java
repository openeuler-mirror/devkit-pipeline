class LeftCurlyCase
{ // violation, ''{' at column 1 should be on the previous line.'
    private interface TestInterface
    { // violation, ''{' at column 3 should be on the previous line.'
    }

    private
    class
    MyClass { // OK
    }

    enum Colors {RED, // OK
        BLUE,
        GREEN;
    }
}