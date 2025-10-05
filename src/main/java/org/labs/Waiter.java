package org.labs;

import static org.labs.Utils.POISON;

// официант: берёт заявки из очереди и доливает суп
final class Waiter implements Runnable {
    final int id;
    final Table table;

    Waiter(int id, Table t) { this.id = id; this.table = t; }

    @Override public void run() {
        try {
            for (RefillRequest req = table.refillQueue.take(); req != POISON; req = table.refillQueue.take()) {
                int want = table.cfg.bitesInBowl();
                int got = 0;

                while (true) {
                    long cur = table.stock.get();
                    if (cur <= 0) break;
                    int take = (int) Math.min(cur, want);
                    if (table.stock.compareAndSet(cur, cur - take)) {
                        got = take;
                        break;
                    }
                }

                if (got > 0) {
                    table.bowls[req.philosopherId].fill(got);
                    req.completed.complete(true);
                    // если налили неполную миску — склад исчерпан, можно пометить закрытие
                    if (got < want) table.closed.set(true);
                } else {
                    table.closed.set(true);
                    req.completed.complete(false);
                }

                table.refillRequested[req.philosopherId].set(false);
                table.pendingRefills.decrementAndGet();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
