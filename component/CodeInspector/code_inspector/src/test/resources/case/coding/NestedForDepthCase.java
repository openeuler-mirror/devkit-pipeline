public class NestedForDepth {
    public void test(){
        for(int i=0; i<10; i++) {
            for(int j=0; j<i; j++) {
                for(int k=0; k<j; k++) {
                    for(int m=0; m<k; m++) { // violation, max allowed nested loop number is 1
                    }
                }
            }
        }

        for(int i=0; i<10; i++) {
            for(int j=0; j<i; j++) { // ok
            }
        }
    }
}