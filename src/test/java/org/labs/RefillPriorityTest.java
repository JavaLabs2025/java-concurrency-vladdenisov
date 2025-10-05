package org.labs;

import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

class RefillPriorityTest {

    @Test
    void lowerEatenComesFirst() {
        RefillRequest a = new RefillRequest(0, 5, 100);
        RefillRequest b = new RefillRequest(1, 2, 200);
        RefillRequest c = new RefillRequest(2, 7, 300);

        PriorityQueue<RefillRequest> pq = new PriorityQueue<>(RefillRequest::compareTo);
        pq.add(a); pq.add(b); pq.add(c);

        // ожидаем порядок по возрастанию eatenSnapshot: b(2), a(5), c(7)
        assertSame(b, pq.poll());
        assertSame(a, pq.poll());
        assertSame(c, pq.poll());
    }

    @Test
    void stableOrderWhenEatenEqual_usesSeq() {
        // одинаковые eatenSnapshot, разный seq — должен побеждать меньший seq
        RefillRequest r1 = new RefillRequest(10, 4, 1);
        RefillRequest r2 = new RefillRequest(11, 4, 2);
        RefillRequest r3 = new RefillRequest(12, 4, 3);

        PriorityQueue<RefillRequest> pq = new PriorityQueue<>(RefillRequest::compareTo);
        pq.add(r3); pq.add(r1); pq.add(r2);

        assertSame(r1, pq.poll());
        assertSame(r2, pq.poll());
        assertSame(r3, pq.poll());
    }
}
