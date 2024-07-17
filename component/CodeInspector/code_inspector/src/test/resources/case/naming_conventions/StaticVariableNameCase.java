public class StaticVariableNameCase {
    public static int goodStatic = 2;
    private static int badStatic = 2;
    public static int ItStatic1 = 2; // violation, 'must match pattern'
    protected static int ItStatic2 = 2; // violation, 'must match pattern'
    private static int ItStatic = 2; // violation, 'must match pattern'
    public static int it_static = 2; // violation, 'must match pattern'
    public static int It_Static = 2; // violation, 'must match pattern'
    private static int It_Static1 = 2; // violation, 'must match pattern'
    static int It_Static2 = 2; // violation, 'must match pattern'
    public static final int IT_STATIC = 2;
}