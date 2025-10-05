package org.labs;

import java.util.concurrent.CompletableFuture;

// запрос на долив порции
final class RefillRequest {
    final int programmerId;
    final long programmerEaten;
    final CompletableFuture<Boolean> completed = new CompletableFuture<>();
    final long seq;
    RefillRequest(int id, long programmerEaten, long seq) {
        this.programmerId = id;
        this.programmerEaten = programmerEaten;
        this.seq = seq;
    }
    public int compareTo(RefillRequest o) {
        // сначала по количеству съеденного, потом по порядковому номеру заявки
        int firstComp = Long.compare(this.programmerEaten, o.programmerEaten);
        if (firstComp != 0) {
            return firstComp;
        }
        return Long.compare(this.seq, o.seq);
    }
}
