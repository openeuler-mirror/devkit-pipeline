import java.io.EOFException; import java.io.BufferedReader;
;; //two empty statements on the same line.
public class OneStatementPerLineCase {
    public void test(){
        //Each line causes violation:
        int var1; int var2;
        var1 = 1; var2 = 2;
        int var1 = 1; int var2 = 2;
        var1++; var2++;
        Object obj1 = new Object(); Object obj2 = new Object();


        //Multi-line statements:
        int var1 = 1
                ; var2 = 2; //violation here
        int o = 1, p = 2,
                r = 5; int t; //violation here
        OutputStream s1 = new PipedOutputStream();
        OutputStream s2 = new PipedOutputStream();
        // only one statement(variable definition) with two variable references
        try (s1; s2; OutputStream s3 = new PipedOutputStream();) // OK
        {}
        // two statements with variable definitions
        try (Reader r = new PipedReader(); s2; Reader s3 = new PipedReader() // violation
        ) {}
    }
}