public class RecordComponentNameCase {
    record MyRecord1(String value, int otherComponentName) {} // OK
    record MyRecord2(String... Values) {} // violation, the record component name
    // should match the regular expression "^[a-z][a-zA-Z0-9]*$"
    record MyRecord3(double my_number) {} // violation, the record component name
    // should match the regular expression "^[a-z][a-zA-Z0-9]*$"
    record MyRecord1(String value, int other) {} // OK
    record MyRecord2(String... strings) {} // OK
    record MyRecord3(double myNumber) {} // violation, the record component name
    // should match the regular expression "^[a-z]+$"
}