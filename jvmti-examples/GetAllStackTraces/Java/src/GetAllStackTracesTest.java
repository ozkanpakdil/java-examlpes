public class GetAllStackTracesTest {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        Thread list[] = new Thread[10];
        for (int i = 0; i < 10; i++) {
            list[i] = new Thread("" + i) {
                public void run() {
                    System.out.println("Thread: " + getName() + " running"+ANSI_BLACK);
                    /*
                     * if(Integer.parseInt(getName())%3==0)
                     * this.interrupt();
                     * if(Integer.parseInt(getName())%2==0)
                     * this.stop();
                     * try {
                     * Thread.sleep(1);
                     * } catch (InterruptedException e) {
                     * e.printStackTrace();
                     * }
                     */

                }
            };
            list[i].start();
        }
        System.out.println("heloooo");
        while (true) {
            System.out.println(ANSI_BLACK+"from jvm:" + list[5].getState());
            Thread.sleep(1000);
        }

        // methodStack1();
    }

    private static void methodStack1() {
        methodStack2();
    }

    private static void methodStack2() {
        methodStack3();
    }

    private static void methodStack3() {
        methodStack4();
    }

    private static void methodStack4() {
        methodStack5();
    }

    private static void methodStack5() {
        exceptionMethod();
    }

    private static void exceptionMethod() {
        try {
            throw new Exception("Throwing an exception...");
        } catch (Exception ex) {
            System.err.println("Exception thrown in Java code...");
        }
    }
}
