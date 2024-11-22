package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
open class CubeGraphBenchmark {
    protected var arr: Graph? = null
    protected lateinit var result: IntArray
    protected val edgeSize = 500
}