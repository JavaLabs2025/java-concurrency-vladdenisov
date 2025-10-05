package org.labs;

import java.util.Arrays;
import java.util.stream.IntStream;

final class SimulationResult {
    public final long[] eatenPerPhilosopher; // сколько съел каждый
    public final long totalEaten;            // сумма съеденного
    public final long stockLeft;             // остаток на складе
    public final boolean closed;             // ресторан закрыт по завершении
    public final int pendingRefills;         // активных заявок не осталось
    public final int N;
    public final int W;
    public final long F;

    public SimulationResult(Table table, Config cfg) {
        this.N = cfg.N();
        this.W = cfg.W();
        this.F = cfg.F();
        this.eatenPerPhilosopher = IntStream.range(0, cfg.N())
                .mapToLong(i -> table.eaten[i].get())
                .toArray();
        this.totalEaten = Arrays.stream(eatenPerPhilosopher).sum();
        this.stockLeft = table.stock.get();
        this.closed = table.closed.get();
        this.pendingRefills = table.pendingRefills.get();
    }
}