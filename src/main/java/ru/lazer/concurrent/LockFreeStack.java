package ru.lazer.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class LockFreeStack<E> implements ConcurrentStack<E> {

    private static final class Node<E> {
        final E value;
        final Node<E> next;

        Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }

    private final AtomicReference<Node<E>> head = new AtomicReference<>(null);

    private final AtomicInteger approxSize = new AtomicInteger(0);

    @Override
    public void push(E value) {
        if (value == null) throw new IllegalArgumentException("value must not be null");

        while (true) {
            Node<E> h = head.get();
            Node<E> n = new Node<>(value, h);
            if (head.compareAndSet(h, n)) {
                approxSize.incrementAndGet();
                return;
            }
        }
    }

    @Override
    public E pop() {
        while (true) {
            Node<E> h = head.get();
            if (h == null) return null;
            Node<E> next = h.next;
            if (head.compareAndSet(h, next)) {
                approxSize.decrementAndGet();
                return h.value;
            }
        }
    }

    @Override
    public int size() {
        return approxSize.get();
    }
}
