package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import java.util.concurrent.ForkJoinPool

open class ParallelBfsBenchmark : CubeGraphBenchmark() {
    private val pool = ForkJoinPool(parallelism)

    @Setup(Level.Iteration)
    fun init() {
        arr = null
        Runtime.getRuntime().gc()
        Thread.sleep(1000)

        arr = CubeGraph(edgeSize)
        result = IntArray(arr!!.size)

        Runtime.getRuntime().gc()
        Thread.sleep(1000)
    }

    @Benchmark
    fun parallelBfsBenchmark() {
        pool.parallelBfs(0, arr!!, result)
    }

    @TearDown
    fun tearDown() {
        pool.close()
    }
}