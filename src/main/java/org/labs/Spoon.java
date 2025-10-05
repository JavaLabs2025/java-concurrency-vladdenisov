package org.labs;

import java.util.concurrent.locks.ReentrantLock;

// уникальная ложка, которую можно залочить
final class Spoon {
    final int id;
    final ReentrantLock lock = new ReentrantLock(true); // fair = true
    Spoon(int id) { this.id = id; }
}