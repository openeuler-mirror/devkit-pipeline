public class ArrayTypeStyleCase {
    int[] nums; // ok
    String strings[]; // violation as follows C style since 'javaStyle' set to false

    char[] toCharArray() { // OK
        return null;
    }

    byte getData()[] { // violation, 'Array brackets at illegal position'
        return null;
    }
}