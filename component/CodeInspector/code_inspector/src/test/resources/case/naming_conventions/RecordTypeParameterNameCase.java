public class RecordTypeParameterNameCase {
    record MyRecord1<T> {} // OK
    record MyRecord2<t> {} // OK
    record MyRecord3<abc> {} // violation, the record type parameter
    // name should match the regular expression "^[a-zA-Z]$"
}