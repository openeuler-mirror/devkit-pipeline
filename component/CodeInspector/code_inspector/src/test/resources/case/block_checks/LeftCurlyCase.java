import java.util.ArrayList;
import java.util.List;

class LeftCurlyCase
{ // violation, ''{' at column 1 should be on the previous line.'
    private interface TestInterface
    { // violation, ''{' at column 3 should be on the previous line.'

    }

    public void test(int index){
        String[] arr =  {"a","b","c"}; // no violation
        String[] arr2 =  new String[]{"a","b","c"}; // no violation
        List<String> numbers = new ArrayList<>();
        numbers.forEach((n) -> System.out.println(n));
        numbers.forEach((n) -> { System.out.println(n); }); //  violation
        numbers.forEach((n) ->
        { System.out.println(n); //  violation
        });
        switch (index)  {
            case 0:{break;} //  violation
            default:
            {break;} //  violation
        }

    }

    private
    class
    MyClass { // OK
    }

    enum Colors {RED, // violation
        BLUE,
        GREEN;
    }
}