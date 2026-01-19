package ru.lazer.concurrent;

public interface ConcurrentStack<E> {
    void push(E value);
    E pop();
    int size();
}
