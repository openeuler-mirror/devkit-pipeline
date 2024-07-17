public class AbbreviationAsWordInNameCase {
    int CURRENT_COUNTER; // violation 'no more than '4' consecutive capital letters'
    static int GLOBAL_COUNTER; // OK, static is ignored
    final Set<String> stringsFOUND = new HashSet<>(); // OK, final is ignored

    @Override
    public void printCOUNTER() { // OK, overridden method is ignored
        System.out.println(CURRENT_COUNTER);
    }

    // violation below 'no more than '4' consecutive capital letters'
    void incrementCOUNTER() {
        CURRENT_COUNTER++; // OK, only definitions are checked
    }

    // violation below 'no more than '4' consecutive capital letters'
    static void incrementGLOBAL() {
        GLOBAL_COUNTER++; // OK, only definitions are checked
    }
}