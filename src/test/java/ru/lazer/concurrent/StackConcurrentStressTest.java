package ru.lazer.concurrent;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Многопоточный стресс-тест:
 * - несколько потоков делают push уникальных значений
 * - несколько потоков делают pop до тех пор, пока не извлекут ожидаемое количество элементов
 * Проверяем: нет потерь/дубликатов.
 */
public class StackConcurrentStressTest {

    @Test
    void synchronized_stack_no_loss_no_duplicates() throws Exception {
        stress(new SynchronizedStack<>());
    }

    @Test
    void lockfree_stack_no_loss_no_duplicates() throws Exception {
        stress(new LockFreeStack<>());
    }

    private static void stress(ConcurrentStack<Integer> stack) throws Exception {
        final int producers = 4;
        final int consumers = 4;
        final int perProducer = 50_000;
        final int total = producers * perProducer;

        ExecutorService pool = Executors.newFixedThreadPool(producers + consumers);

        // уникальные значения: [0..total-1]
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);

        ConcurrentHashMap<Integer, Boolean> seen = new ConcurrentHashMap<>();

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch prodDone = new CountDownLatch(producers);

        // producers
        for (int p = 0; p < producers; p++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perProducer; i++) {
                        int v = produced.getAndIncrement();
                        stack.push(v);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    prodDone.countDown();
                }
            });
        }

        // consumers
        for (int c = 0; c < consumers; c++) {
            pool.submit(() -> {
                try {
                    start.await();
                    while (true) {
                        int cur = consumed.get();
                        if (cur >= total) break;

                        Integer v = stack.pop();
                        if (v == null) {
                            // если продюсеры ещё работают — покрутимся
                            if (prodDone.getCount() > 0) {
                                Thread.onSpinWait();
                                continue;
                            }
                            // продюсеры завершились, но стек пуст — небольшой yield и ещё попытка
                            Thread.yield();
                            continue;
                        }

                        // фиксируем полученное значение
                        Boolean prev = seen.putIfAbsent(v, Boolean.TRUE);
                        assertNull(prev, "Duplicate value popped: " + v);
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        start.countDown();

        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "Timeout: threads did not finish");

        assertEquals(total, produced.get(), "Not all values produced");
        assertEquals(total, consumed.get(), "Not all values consumed");
        assertEquals(total, seen.size(), "Lost values (set size mismatch)");
        // все значения должны быть в диапазоне
        assertTrue(seen.keySet().stream().allMatch(v -> v >= 0 && v < total), "Out-of-range values observed");
    }
}
