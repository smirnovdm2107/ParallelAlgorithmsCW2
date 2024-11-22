package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import java.util.concurrent.ForkJoinPool

open class ParallelBfs2Benchmark : CubeGraphBenchmark() {
    private val parallelism = 4
    private val pool = ForkJoinPool(parallelism)
    protected lateinit var frontier: IntArray
    protected lateinit var nextFrontier: IntArray
    protected lateinit var sandbox: IntArray
    protected lateinit var degs: IntArray

    @Setup(Level.Iteration)
    fun init() {
        arr = null
        Runtime.getRuntime().gc()
        Thread.sleep(1000)

        arr = CubeGraph(edgeSize)
        result = IntArray(arr!!.size)
        frontier = IntArray(arr!!.size)
        nextFrontier = IntArray(arr!!.edgeSize)
        sandbox = IntArray(arr!!.edgeSize)
        degs = IntArray(arr!!.size)

        Runtime.getRuntime().gc()
        Thread.sleep(1000)
    }

    @Benchmark
    fun parallelBfs2Benchmark() {
        pool.parallelBfs2(0, arr!!, result, frontier, nextFrontier, sandbox, degs)
    }

    @TearDown
    fun tearDown() {
        pool.close()
    }
}