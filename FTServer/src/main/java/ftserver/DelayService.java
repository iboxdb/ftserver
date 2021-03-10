package ftserver;

public class DelayService {

    public static void delayIndex(long time) {
        pageIndexDelay = System.currentTimeMillis() + time;
    }

    public static void delayIndex() {
        delayIndex(5L * 1000L);
    }
    private static long pageIndexDelay = Long.MIN_VALUE;

    public static void delay() {
        if (pageIndexDelay == Long.MIN_VALUE) {
            return;
        }

        while (System.currentTimeMillis() < pageIndexDelay) {
            long d = pageIndexDelay - System.currentTimeMillis();
            if (d < 0) {
                d = 0;
            }
            if (d > (120 * 1000)) {
                d = 120 * 1000;
            }
            try {
                Thread.sleep(d);
            } catch (Throwable ex) {
            }
        }
    }

}
