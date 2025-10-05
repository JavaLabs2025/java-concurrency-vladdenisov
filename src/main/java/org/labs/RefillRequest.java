package org.labs;

import java.util.concurrent.CompletableFuture;

// запрос на долив порции
final class RefillRequest {
    final int philosopherId;
    final long philosopherEaten;
    final CompletableFuture<Boolean> completed = new CompletableFuture<>();
    final long seq;
    RefillRequest(int id, long philosopherEaten, long seq) {
        this.philosopherId = id;
        this.philosopherEaten = philosopherEaten;
        this.seq = seq;
    }
    public int compareTo(RefillRequest o) {
        // сначала по количеству съеденного, потом по порядковому номеру заявки
        int firstComp = Long.compare(this.philosopherEaten, o.philosopherEaten);
        if (firstComp != 0) {
            return firstComp;
        }
        return Long.compare(this.seq, o.seq);
    }
}
