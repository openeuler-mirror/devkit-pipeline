class ParenPadCase {
    int n;

    public void fun() {
        bar( 1);  // violation 'is followed by whitespace'
    }

    public void bar(int k ) {  // violation 'is preceded with whitespace'
        while (k > 0) {
        }

        StringBuilder obj = new StringBuilder(k);
    }

    public void fun2() {
        switch( n) {  // violation 'is followed by whitespace'
            case 2:
                bar(n);
            default:
                break;
        }
    }
}