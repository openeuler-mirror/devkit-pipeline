class MethodParamPadCase {
    public Example1() {
        super();
    }

    public Example1 (int aParam) { // violation ''(' is preceded with whitespace'
        super (); // violation ''(' is preceded with whitespace'
    }

    public void method() {}

    public void methodWithVeryLongName
            () {} // violation ''(' should be on the previous line.'
}