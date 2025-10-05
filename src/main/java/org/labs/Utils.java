package org.labs;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    // "ядовитая" ложка для остановки потоков
    static final RefillRequest POISON = new RefillRequest(-1, -1, -1);

    static void sleepRandom(ThreadLocalRandom rnd, Duration min, Duration max) {
        long d = rnd.nextLong(min.toMillis(), Math.max(min.toMillis()+1, max.toMillis()+1));
        try { Thread.sleep(d); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
