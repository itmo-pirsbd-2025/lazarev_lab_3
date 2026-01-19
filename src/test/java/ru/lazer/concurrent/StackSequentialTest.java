package ru.lazer.concurrent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackSequentialTest {

    @Test
    void synchronized_stack_lifo() {
        ConcurrentStack<Integer> s = new SynchronizedStack<>();
        s.push(1);
        s.push(2);
        s.push(3);
        assertEquals(3, s.pop());
        assertEquals(2, s.pop());
        assertEquals(1, s.pop());
        assertNull(s.pop());
    }

    @Test
    void lockfree_stack_lifo_single_thread() {
        ConcurrentStack<Integer> s = new LockFreeStack<>();
        s.push(10);
        s.push(20);
        s.push(30);
        assertEquals(30, s.pop());
        assertEquals(20, s.pop());
        assertEquals(10, s.pop());
        assertNull(s.pop());
    }
}
