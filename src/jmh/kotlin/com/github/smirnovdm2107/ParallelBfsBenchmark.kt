package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.Benchmark
import java.util.concurrent.ForkJoinPool

open class ParallelBfsBenchmark : CubeGraphBenchmark() {
    private val parallelism = 4
    private val pool = ForkJoinPool(parallelism)

    @Benchmark
    fun parallelBfsBenchmark() {
        pool.parallelBfs(0, arr, 100)
    }
}