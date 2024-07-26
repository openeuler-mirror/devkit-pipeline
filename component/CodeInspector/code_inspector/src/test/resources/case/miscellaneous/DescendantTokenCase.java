public class DescendantTokenCase {

    public void foo1() {
        int x = 1;
        int y = 2;
        int optimistic = 2;
        switch (y) {
            case 1:
                switch (y) {
                    case 2:
                        switch (y) {
                            case 2:

                                break;
                        }
                        break;
                }
                break;
        }
        if (optimistic) {
            message = "half full";
        } else {
            if (optimistic) {
                message = "half full";
            } else {
                if (optimistic) {
                    message = "half full";
                }
                else {
                    switch (y) { // violation
                        case 2:
                            System.out.println("xyz");
                            break;
                    }
                    message = "half empty";
                }
                message = "half empty";
            }
            message = "half empty";
        }

        switch (y) {
            case 1:
                switch (y) {
                    case 2:
                        System.out.println("xyz");
                        break;
                }
                break;
        }
        switch (x) { // ok
            case 1:
                System.out.println("xyz");
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
            try { //
                // Some code
            } catch (Exception ex) {
                // handle exception
            }
        } finally {
            try { //
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
        for (; j != array.length; ) { //
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