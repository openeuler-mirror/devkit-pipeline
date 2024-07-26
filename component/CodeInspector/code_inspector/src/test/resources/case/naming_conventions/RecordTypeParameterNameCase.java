public class RecordTypeParameterNameCase {
    record MyRecord1<T>() {} // OK
    record MyRecord2<t>() {} // violation
    record MyRecord3<abc>() {} // violation, the record type parameter
}