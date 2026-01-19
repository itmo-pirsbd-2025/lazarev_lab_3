package ru.lazer.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ru.lazer.concurrent.ConcurrentStack;
import ru.lazer.concurrent.LockFreeStack;
import ru.lazer.concurrent.SynchronizedStack;

import java.util.concurrent.TimeUnit;

/**
 * Запуск:
 *   mvn clean package
 *   java -jar target/benchmarks.jar StackJmhBenchmark -t 8
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 7, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class StackJmhBenchmark {

    @State(Scope.Benchmark)
    public static class SharedState {
        @Param({"SYNC", "LOCK_FREE"})
        public String impl;

        public ConcurrentStack<Integer> stack;

        @Setup(Level.Trial)
        public void setup() {
            stack = "SYNC".equals(impl) ? new SynchronizedStack<>() : new LockFreeStack<>();
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {
        int x = 1;

        int next() {
            // детерминированный LCG вместо Random (меньше шума)
            x = x * 1103515245 + 12345;
            return x;
        }
    }

    @Benchmark
    public void pushPop(SharedState s, ThreadState t, Blackhole bh) {
        int v = t.next();
        s.stack.push(v);
        Integer r = s.stack.pop();
        bh.consume(r);
    }
}
