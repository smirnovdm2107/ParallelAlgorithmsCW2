package com.github.smirnovdm2107

import org.openjdk.jmh.annotations.Benchmark


open class SequentialBfsBenchmark : CubeGraphBenchmark() {
    @Benchmark
    fun sequentialBfsBenchmark() {
        sequentialBfs(0, arr)
    }
}