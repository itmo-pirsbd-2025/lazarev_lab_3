package ru.lazer.jmh;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public final class RunBenchmarks {

    private RunBenchmarks() {}

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StackJmhBenchmark.class.getSimpleName())
                .forks(1)
                .threads(8)
                .build();

        new Runner(opt).run();
    }
}
