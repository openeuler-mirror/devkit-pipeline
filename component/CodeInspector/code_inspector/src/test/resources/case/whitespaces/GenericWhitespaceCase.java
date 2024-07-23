
class GenericWhitespaceCase{
    // Generic methods definitions
    public <K, V extends Number> void foo(K k, V v) {}

    // Generic type definition
    class name<T1, T2, Tn> {}
    // Record header
    record License<T>() {}

    Map<Integer, String>m2; // violation, ">" not followed by whitespace
    Pair<Integer, Integer > p2; // violation, ">" preceded with whitespace

    record License2<T> () {} // violation, ">" followed by whitespace

    List< String> l; // violation, "<" followed by whitespace
    Box b = Box. <String>of("foo"); // violation, "<" preceded with whitespace
    public<T> void foo() {} // violation, "<" not preceded with whitespace

    List a = new ArrayList<> (); // violation, ">" followed by whitespace

    public <T> void demo(){
        // Generic type reference
        OrderedPair<String, Box<Integer>> p;
        // Generic preceded method name
        boolean same = Util.<Integer, String>compare(p1, p2);
        // Diamond operator
        Pair<Integer, String> p1 = new Pair<>(1, "apple");
        // Method reference
        List<T> list = ImmutableList.Builder<T>::new;
        // Method reference
        sort(list, Comparable::<T>compareTo);
        // Constructor call
        MyClass obj = new <String>MyClass();
    }
}
