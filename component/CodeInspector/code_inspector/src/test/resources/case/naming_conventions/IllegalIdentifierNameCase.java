public class IllegalIdentifierNameCase {
    Integer _ = 4; // violation, 'Name '_' must match pattern'
    Integer _hello = 4;
    Integer record = 4;
    Integer _record = 4;
}