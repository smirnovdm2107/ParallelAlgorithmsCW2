package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup


open class SequentialBfsBenchmark : CubeGraphBenchmark() {

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
    fun sequentialBfsBenchmark() {
        sequentialBfs(0, arr!!, result)
    }
}