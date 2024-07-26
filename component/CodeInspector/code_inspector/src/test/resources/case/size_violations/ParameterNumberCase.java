public class FileLengthCase {
    public FileLengthCase(String param1, String param2,String param3,String param4,String param5,
                          String param6,String param7) {
    }
    public FileLengthCase(String param1, String param2,String param3,String param4,String param5,
                        String param6,String param7,String param8) { // violation
    }

    void MyMethod(String param1, String param2,String param3,String param4,String param5) {
    }

    void MyMethod(String param1, String param2,String param3,String param4,String param5,String param6) { // violation
    }
}