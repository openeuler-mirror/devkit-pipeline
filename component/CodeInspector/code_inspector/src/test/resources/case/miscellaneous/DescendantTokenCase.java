public class DescendantTokenCase {

    public void foo1() {
        int x = 1;
        int y = 2;
        switch (x) { // ok
            case 1:
                System.out.println("xyz");
                break;
        }
        switch (y) { // violation
            case 1:
                switch (y) {
                    case 2:
                        System.out.println("xyz");
                        break;
                }
                break;
        }
    }

    public void foo2() {
        try {
            // Some code
        } catch (Exception e) { // ok
            System.out.println("xyz");
            return;
        } finally { // ok
            System.out.println("xyz");
        }
        try {
            // Some code
        } catch (Exception e) {
            try { // violation
                // Some code
            } catch (Exception ex) {
                // handle exception
            }
        } finally {
            try { // violation
                // Some code
            } catch (Exception e) {
                // handle exception
            }
        }
    }

    public void foo3() {
        int[] array = new int[]{1, 2, 3, 4, 5};

        for (int i = 0; i != array.length; i++) { // ok
            System.out.println(i);
        }

        int j = 0;
        for (; j != array.length; ) { // violation
            System.out.println(j);
            j++;
        }
    }

    public void foo4() {
        for (int i = 0; i != 10; i++) { // ok
            System.out.println(i);
        }
        int k = 0;
        for (; ; ) { // violation
            System.out.println(k);
        }
    }
}