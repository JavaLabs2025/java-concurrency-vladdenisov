package org.labs;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class DiningProgrammersSimulation {
    public static SimulationResult runSimulation(Config cfg) throws Exception {
        Table table = new Table(cfg);

        // барьер для одновременного старта философов
        CyclicBarrier startBarrier = new CyclicBarrier(cfg.N());

        // официанты (виртуальные потоки)
        Thread[] waiters = new Thread[cfg.W()];
        for (int id = 0; id < cfg.W(); id++) {
            waiters[id] = Thread.startVirtualThread(new Waiter(id, table));
        }

        // философы (виртуальные потоки)
        Thread[] philosophers = new Thread[cfg.N()];
        for (int i = 0; i < cfg.N(); i++) {
            final int id = i;
            philosophers[i] = Thread.startVirtualThread(() -> {
                try { startBarrier.await(); } catch (Exception ignored) {}
                new Philosopher(id, table).run();
            });
        }

        // ждём логического завершения (еда кончилась и миски пусты)
        while (true) {
            if (table.isFinished()) {
                table.closed.set(true);
                break;
            }
            Thread.sleep(10);
        }

        // корректно завершаем официантов
        for (int i = 0; i < cfg.W(); i++) {
            table.refillQueue.put(Utils.POISON);
        }

        // ждём всех
        for (Thread t : philosophers) t.join();
        for (Thread w : waiters) w.join();

        return new SimulationResult(table, cfg);
    }

    private static Config getCfg() {
        final int N = 7; // number of programmers
        final int W = 3; // number of waiters
        final long F = 5000; // total portions in stock
        final Duration thinkMin = Duration.ofMillis(2); // min thinking time in ms
        final Duration thinkMax = Duration.ofMillis(8); // max thinking time in ms
        final Duration eatMin = Duration.ofMillis(2); // min eating time in ms
        final Duration eatMax = Duration.ofMillis(6); // max eating time in ms
        final int bitesInBowl = 1; // portions in one bowl

        return new Config(N, W, F, bitesInBowl, thinkMin, thinkMax, eatMin, eatMax);
    }

    public static void main(String[] args) throws Exception {
        Config cfg = getCfg();
        SimulationResult res = runSimulation(cfg);

        // печать результатов
        System.out.println("\n=== RESULT ===");
        System.out.printf("Total eaten: %d, stock left: %d (initial %d)%n",
                res.totalEaten, res.stockLeft, res.F);
        System.out.println("Per programmer portions:");
        for (int i = 0; i < res.N; i++) {
            System.out.printf("  #%d: %d%n", i, res.eatenPerPhilosopher[i]);
        }
        long max = Arrays.stream(res.eatenPerPhilosopher).max().orElse(0);
        long min = Arrays.stream(res.eatenPerPhilosopher).min().orElse(0);
        double avg = res.N == 0 ? 0 : (double) res.totalEaten / res.N;
        double spreadPercent = avg == 0 ? 0 : (double)(max - min) / avg * 100.0;
        System.out.printf("Min=%d  Max=%d  Avg=%.2f  Spread=%d, SpreadPercent=%.2f%%%n",
                min, max, avg, (max - min), spreadPercent);
        System.out.println("Finished.");
    }
}
