package org.labs;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;

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

    private static Config loadConfig() {
        Properties props = new Properties();
        try (InputStream in = DiningProgrammersSimulation.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (in == null) {
                throw new RuntimeException("Не найден application.properties в classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения application.properties", e);
        }

        int N = Integer.parseInt(props.getProperty("N", "7"));
        int W = Integer.parseInt(props.getProperty("W", "3"));
        long F = Long.parseLong(props.getProperty("F", "5000"));
        int bitesInBowl = Integer.parseInt(props.getProperty("bitesInBowl", "1"));

        Duration thinkMin = Duration.ofMillis(Long.parseLong(props.getProperty("thinkMin", "2")));
        Duration thinkMax = Duration.ofMillis(Long.parseLong(props.getProperty("thinkMax", "8")));
        Duration eatMin = Duration.ofMillis(Long.parseLong(props.getProperty("eatMin", "2")));
        Duration eatMax = Duration.ofMillis(Long.parseLong(props.getProperty("eatMax", "6")));

        return new Config(N, W, F, bitesInBowl, thinkMin, thinkMax, eatMin, eatMax);
    }

    public static void main(String[] args) throws Exception {
        Config cfg = loadConfig();
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
