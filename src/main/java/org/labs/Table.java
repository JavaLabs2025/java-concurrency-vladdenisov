package org.labs;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

// глобальный контекст, по сути сам ресторан
final class Table {
    final Config cfg;
    final Spoon[] spoons;
    final Semaphore butler; // "дворецкий", разрешает максимум N−1 философов одновременно
    final PriorityBlockingQueue<RefillRequest> refillQueue; // очередь заявок на доливку
    final AtomicLong stock; // остаток порций на складе
    final AtomicBoolean closed = new AtomicBoolean(false); // флаг "ресторан закрыт"
    final Bowl[] bowls; // миски философов
    final AtomicLong[] eaten; // сколько каждый съел
    final AtomicInteger pendingRefills = new AtomicInteger(0); // количество активных заявок
    final AtomicBoolean[] refillRequested; // флаг, что философ уже запросил доливку
    final AtomicLong sequence = new AtomicLong(0); // для нумерации заявок

    Table(Config cfg) {
        this.cfg = cfg;
        this.spoons = IntStream.range(0, cfg.N()).mapToObj(Spoon::new).toArray(Spoon[]::new);
        this.butler = new Semaphore(Math.max(1, cfg.N() - 1), true);
        this.refillQueue = new PriorityBlockingQueue<>(cfg.N() * 2, RefillRequest::compareTo);
        this.stock = new AtomicLong(cfg.F());
        this.bowls = IntStream.range(0, cfg.N()).mapToObj(_ -> new Bowl()).toArray(Bowl[]::new);
        this.eaten = IntStream.range(0, cfg.N()).mapToObj(_ -> new AtomicLong()).toArray(AtomicLong[]::new);
        this.refillRequested = IntStream.range(0, cfg.N()).mapToObj(_ -> new AtomicBoolean(false)).toArray(AtomicBoolean[]::new);
    }

    // проверяем, закончилась ли симуляция (еда кончилась и миски пусты)
    boolean isFinished() {
        if (stock.get() > 0) return false;
        if (pendingRefills.get() > 0) return false;
        for (Bowl b : bowls) if (!b.isEmpty()) return false;
        return true;
    }
}