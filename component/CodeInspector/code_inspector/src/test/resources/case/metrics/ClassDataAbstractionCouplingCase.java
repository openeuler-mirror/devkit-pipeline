public class ClassDataAbstractionCouplingCase {
    Set set = new HashSet(); // HashSet ignored due to default excludedClasses property
    Map map = new HashMap(); // HashMap ignored due to default excludedClasses property
    Date date = new Date(); // Counted, 1
    Time time = new Time(); // Counted, 2
    Place place = new Place(); // Counted, 3
}