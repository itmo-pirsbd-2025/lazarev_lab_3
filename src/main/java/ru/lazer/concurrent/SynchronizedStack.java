package ru.lazer.concurrent;

import java.util.ArrayDeque;

public final class SynchronizedStack<E> implements ConcurrentStack<E> {

    private final ArrayDeque<E> deque = new ArrayDeque<>();

    @Override
    public synchronized void push(E value) {
        if (value == null) throw new IllegalArgumentException("value must not be null");
        deque.push(value);
    }

    @Override
    public synchronized E pop() {
        return deque.pollFirst();
    }

    @Override
    public synchronized int size() {
        return deque.size();
    }
}
