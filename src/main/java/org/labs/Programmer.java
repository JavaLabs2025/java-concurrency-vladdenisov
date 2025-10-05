package org.labs;

import java.util.concurrent.ThreadLocalRandom;

import static org.labs.Utils.sleepRandom;

final class Programmer implements Runnable {
    final int id;
    final Table table;

    Programmer(int id, Table t) { this.id = id; this.table = t; }

    @Override public void run() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Spoon left  = table.spoons[id];
        Spoon right = table.spoons[(id + 1) % table.cfg.N()];
        // блокировка по возрастанию id для предотвращения дедлока
        Spoon first  = left.id < right.id ? left : right;
        Spoon second = left.id < right.id ? right : left;

        // начальная порция, если на складе ещё есть еда
        ensureBowlHasPortion();

        while (!table.closed.get() && !Thread.currentThread().isInterrupted()) {
            // программист думает перед следующим приёмом пищи
            sleepRandom(rnd, table.cfg.thinkMin(), table.cfg.thinkMax());

            // запрос разрешения у дворецкого (ограничение на N−1 едоков одновременно)
            try {
                table.butler.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            try {
                // берём первую ложку, потом вторую
                first.lock.lock();
                try {
                    second.lock.lock();
                    try {
                        // едим пока есть еда в тарелке
                        int bites = 0;
                        while (table.bowls[id].takeOne()) {
                            table.eaten[id].addAndGet(1);
                            bites++;
                            // кушаем ложку
                            sleepRandom(rnd, table.cfg.eatMin(), table.cfg.eatMax());
                        }
                        // если миска опустела просим официанта долить
                        if (bites == 0 && !table.closed.get()) {
                            requestRefill();
                        }
                    } finally {
                        second.lock.unlock();
                    }
                } finally {
                    // освобождаем вторую ложку, потом первую ложку
                    first.lock.unlock();
                }
            } finally {
                // отпускаем дворецкого
                table.butler.release();
            }
        }
    }

    // проверяем, есть ли еда в миске, и если нет — сразу запрашиваем доливку
    void ensureBowlHasPortion() {
        if (!table.closed.get() && table.bowls[id].isEmpty()) {
            requestRefill();
        }
    }

    // запрос доливки у официанта
    void requestRefill() {
        if (table.closed.get()) return;
        if (!table.refillRequested[id].compareAndSet(false, true)) return;
        table.pendingRefills.incrementAndGet();
        RefillRequest req = new RefillRequest(id, table.eaten[id].get(), table.sequence.getAndIncrement());
        try {
            // кладём заявку в очередь
            table.refillQueue.put(req);
            // ждём, пока официант выполнит запрос
            boolean ok = req.completed.join();
            if (!ok) {
                // официант вернул false — еды на складе не осталось
                table.closed.set(true);
            }
        } finally {

        }
    }
}
