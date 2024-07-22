public class PatternVariableNameCase {
    void foo(Object o1){
        if (o1 instanceof String STRING) {} // violation
        if (o1 instanceof Integer num) {}
        if (o1 instanceof Integer num_1) {} // violation
        if (o1 instanceof Integer n) {}
    }
}