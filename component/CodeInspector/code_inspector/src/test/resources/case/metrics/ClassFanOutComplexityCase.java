import java.sql.Time;

public class ClassFanOutComplexityCase {
    Set set = new HashSet(); // Set, HashSet ignored due to default excludedClasses property
    Map map = new HashMap(); // Map, HashMap ignored due to default excludedClasses property
    Date date = new Date(); // Counted, 1
    Time time = new Time(); // Counted, 2
    Place place = new Place(); // Counted, 3
    int value = 10; // int is ignored due to default excludedClasses property

    void method() {
        var result = "result"; // var is ignored due to default excludedClasses property
    }

    public void setTime() {
        Time time1 = new Time();
    }
}