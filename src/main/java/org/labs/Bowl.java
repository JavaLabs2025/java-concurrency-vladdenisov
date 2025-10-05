package org.labs;

import java.util.concurrent.atomic.AtomicInteger;

final class Bowl {
    // количество ложек супа, оставшихся в миске
    final AtomicInteger remaining = new AtomicInteger(0);

    // долить в миску новую порцию
    void fill(int portion) { remaining.addAndGet(portion); }

    // проблем съесть одну ложку
    boolean takeOne() {
        while (true) {
            int cur = remaining.get();
            if (cur <= 0) return false;
            if (remaining.compareAndSet(cur, cur - 1)) return true;
        }
    }

    boolean isEmpty() { return remaining.get() == 0; }
}
