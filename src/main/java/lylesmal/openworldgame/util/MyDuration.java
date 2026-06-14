package lylesmal.openworldgame.util;

public class MyDuration {

    static long startTime = System.currentTimeMillis() / 1000;
    static long endTime = System.currentTimeMillis() / 1000;
    static long duration = 0;

    public MyDuration() {
        startTime = System.currentTimeMillis() / 1000;
        endTime = System.currentTimeMillis() / 1000;
        duration = 0;
    }

    public static void timePeriod(int seconds) {
        while (duration != seconds) {
            endTime = System.currentTimeMillis() / 1000;
            duration = endTime - startTime;
        }
        System.out.println("This build took " + duration + "s to complete");
    }
}
