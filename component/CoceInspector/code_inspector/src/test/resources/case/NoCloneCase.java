public class NoCloneCase {
    public Object clone() {return null;} // violation, overrides the clone method

    public NoCloneCase clone() {return null;} // violation, overrides the clone method

    public static NoCloneCase clone(Object o) {return null;} // OK

    public static NoCloneCase clone(NoCloneCase o) {return null;} // OK
}