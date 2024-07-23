class EmptyForIteratorPadCase {
    Map<String, String> map = new HashMap<>();
    void example() {
        for (Iterator it = map.entrySet().iterator();  it.hasNext(););
        for (Iterator it = map.entrySet().iterator();  it.hasNext(); );
        // violation above '';' is followed by whitespace'

        for (Iterator foo = map.entrySet().iterator();
             foo.hasNext();
        );
    }
}