public class EqualsAvoidNullCase {
    public void test(){
        String nullString = null;
        nullString.equals("My_Sweet_String");            // violation
        nullString == "My_Sweet_String";            // OK
        "My_Sweet_String".equals(nullString);            // OK
        nullString.equalsIgnoreCase("My_Sweet_String");  // violation
        "My_Sweet_String".equalsIgnoreCase(nullString);  // OK
        String notEmpty = "notEmpty";
        nullString.equals(notEmpty); // OK
        nullString == notEmpty;
    }
}