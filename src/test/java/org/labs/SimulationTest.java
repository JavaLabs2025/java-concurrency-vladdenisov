package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SimulationResultTest {

    private static Config cfg(int N, int W, long F) {
        return new Config(
                N, W, F,
                1,
                Duration.ofMillis(1), Duration.ofMillis(3),
                Duration.ofMillis(1), Duration.ofMillis(2)
        );
    }

    @Test
    @Timeout(5)
    void simulationTerminatesAndKeepsInvariants() throws Exception {
        int N = 7, W = 2;
        long F = 500;

        SimulationResult res = DiningProgrammersSimulation.runSimulation(cfg(N, W, F));

        assertTrue(res.closed, "Ресторан должен быть закрыт к окончанию теста");

        assertEquals(N, res.eatenPerProgrammer.length, "Длина eatenPerProgrammer должна быть N");

        assertEquals(0, res.pendingRefills, "Не должно остаться активных заявок на доливку");

        assertTrue(res.stockLeft >= 0, "Остаток на складе не должен быть отрицательным");

        assertEquals(F - res.stockLeft, res.totalEaten,
                "Сумма съеденного должна совпадать с списанным со склада");

        long sumByPeople = Arrays.stream(res.eatenPerProgrammer).sum();
        assertEquals(res.totalEaten, sumByPeople, "totalEaten должен равняться сумме по участникам");
    }

    @Test
    @Timeout(10)
    void fairnessSpreadIsReasonableForPriorityRefills() throws Exception {
        int N = 7, W = 3;
        long F = 5500;

        SimulationResult res = DiningProgrammersSimulation.runSimulation(cfg(N, W, F));

        long max = Arrays.stream(res.eatenPerProgrammer).max().orElse(0);
        long min = Arrays.stream(res.eatenPerProgrammer).min().orElse(0);
        double avg = res.N == 0 ? 0.0 : (double) res.totalEaten / res.N;

        // допускаем умеренный спред
        double allowedSpread = avg * 0.25;
        assertTrue(max - min <= allowedSpread,
                String.format("Справедливость: спред слишком большой (min=%d, max=%d, avg=%.2f, spread=%.2f, allowed=%.2f)",
                        min, max, avg, (double)(max - min), allowedSpread));
    }

    @Test
    @Timeout(4)
    void smallRunNoDeadlock() throws Exception {
        SimulationResult res = DiningProgrammersSimulation.runSimulation(cfg(5, 2, 500));

        assertTrue(res.closed, "Симуляция должна корректно завершиться");
        assertEquals(0, res.pendingRefills, "Очередь доливок должна быть пустой к концу");
    }
}
