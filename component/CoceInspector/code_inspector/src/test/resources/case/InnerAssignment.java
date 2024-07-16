public class InnerAssignment {
    public void test(){
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