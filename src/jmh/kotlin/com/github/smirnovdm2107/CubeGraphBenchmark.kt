package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
open class CubeGraphBenchmark {
    protected lateinit var arr: Graph
    private val edgeSize = 500

    @Setup(Level.Invocation)
    fun init() {
        arr = CubeGraph(edgeSize)
    }
}