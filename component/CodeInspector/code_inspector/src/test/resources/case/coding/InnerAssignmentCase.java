public class InnerAssignment {
    public void test(){
        int a = b = 1;
        String line;
        while ((line = bufferedReader.readLine()) != null) { // OK
            // process the line
        }

        for (;(line = bufferedReader.readLine()) != null;) { // OK
            // process the line
        }

        do {
            // process the line
        }
        while ((line = bufferedReader.readLine()) != null); // OK
    }
}