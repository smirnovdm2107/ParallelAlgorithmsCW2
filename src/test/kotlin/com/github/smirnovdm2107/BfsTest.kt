package com.github.smirnovdm2107

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.ForkJoinPool
import java.util.stream.Stream

class BfsTest {

    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `graph with 1 node`(algo: Algo) {
        val graph = arrayOf(Node(IntArray(0)))
        val result = algo.bfs(0, ArrayGraph(graph))
        Assertions.assertEquals(result.size, 1)
        Assertions.assertEquals(result[0], 0)
    }

    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `graph with 1 edge`(algo: Algo) {
        val graph = arrayOf(
            Node(
                intArrayOf(1)
            ),
            Node(
                intArrayOf(0)
            )
        )
        val result = algo.bfs(0, ArrayGraph(graph))
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(0, result[0])
        Assertions.assertEquals(1, result[1])
    }

    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `graph with 2 edges (distance 1)`(algo: Algo) {
        val graph = arrayOf(
            Node(
                intArrayOf(1, 2)
            ),
            Node(
                intArrayOf(0)
            ),
            Node(
                intArrayOf(0)
            )
        )
        val result = algo.bfs(0, ArrayGraph(graph))
        Assertions.assertEquals(3, result.size)
        Assertions.assertEquals(0, result[0])
        Assertions.assertEquals(1, result[1])
        Assertions.assertEquals(1, result[2])
    }

    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `graph with 2 edges (bamboo)`(algo: Algo) {
        val graph = arrayOf(
            Node(
                intArrayOf(1)
            ),
            Node(
                intArrayOf(0, 2)
            ),
            Node(
                intArrayOf(1)
            )
        )
        val result = algo.bfs(0, ArrayGraph(graph))
        Assertions.assertEquals(3, result.size)
        Assertions.assertEquals(0, result[0])
        Assertions.assertEquals(1, result[1])
        Assertions.assertEquals(2, result[2])
    }

    // 0
    // |  \
    // 1    2
    // | \    \
    // 5  4     3
    // |          \
    // 6            7
    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `tree graph`(algo: Algo) {
        val graph = arrayOf(
            Node(
                intArrayOf(1, 2)
            ),
            Node(
                intArrayOf(0, 5, 4)
            ),
            Node(
                intArrayOf(0, 3)
            ),
            Node(
                intArrayOf(2, 7)
            ),
            Node(
                intArrayOf(1)
            ),
            Node(
                intArrayOf(1, 6)
            ),
            Node(
                intArrayOf(5)
            ),
            Node(
                intArrayOf(3)
            )
        )
        val result = algo.bfs(0, ArrayGraph(graph))
        Assertions.assertEquals(8, result.size)
        Assertions.assertEquals(0, result[0])
        Assertions.assertEquals(1, result[1])
        Assertions.assertEquals(1, result[2])
        Assertions.assertEquals(2, result[3])
        Assertions.assertEquals(2, result[4])
        Assertions.assertEquals(2, result[5])
        Assertions.assertEquals(3, result[6])
        Assertions.assertEquals(3, result[7])
    }


    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `connected graph`(algo: Algo) {
        val graph = arrayOf(
            Node(
                intArrayOf(1, 2, 4, 5)
            ),
            Node(
                intArrayOf(0, 8)
            ),
            Node(
                intArrayOf(0, 4, 7)
            ),
            Node(
                intArrayOf(4, 9)
            ),
            Node(
                intArrayOf(0, 2, 3)
            ),
            Node(
                intArrayOf(0, 6, 8)
            ),
            Node(
                intArrayOf(5, 7)
            ),
            Node(
                intArrayOf(2, 6)
            ),
            Node(
                intArrayOf(1, 5, 9)
            ),
            Node(
                intArrayOf(3, 8)
            )
        )
        val result = algo.bfs(0, ArrayGraph(graph))
        Assertions.assertEquals(result.size, 10)
        Assertions.assertEquals(0, result[0])
        Assertions.assertEquals(1, result[1])
        Assertions.assertEquals(1, result[2])
        Assertions.assertEquals(2, result[3])
        Assertions.assertEquals(1, result[4])
        Assertions.assertEquals(1, result[5])
        Assertions.assertEquals(2, result[6])
        Assertions.assertEquals(2, result[7])
        Assertions.assertEquals(2, result[8])
        Assertions.assertEquals(3, result[9])
    }

    @ParameterizedTest
    @MethodSource("bfsMethods")
    fun `cube graph`(algo: Algo) {
        val edgeSize = 50
        val result = algo.bfs(0, CubeGraph(edgeSize))
        Assertions.assertEquals(result.size, edgeSize * edgeSize * edgeSize)
        for (i1 in 0 until edgeSize) {
            for (i2 in 0 until edgeSize) {
                for (i3 in 0 until edgeSize) {
                    val index = i1 + i2 * edgeSize + i3 * edgeSize * edgeSize
                    Assertions.assertEquals(
                        i1 + i2 + i3,
                        result[index]
                    )
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun bfsMethods() = Stream.of(
            Algo.SequentialBfs,
            Algo.ParallelBfs(4, 2),
            Algo.ParallelBfs(4, 1)
        )
    }


    sealed class Algo {
        abstract fun bfs(start: Int, graph: Graph): IntArray

        data object SequentialBfs : Algo() {
            override fun bfs(start: Int, graph: Graph): IntArray {
                return sequentialBfs(start, graph)
            }
        }

        data class ParallelBfs(
            val parallelism: Int,
            val blockSize: Int
        ) : Algo() {
            override fun bfs(start: Int, graph: Graph): IntArray {
                val pool = ForkJoinPool(parallelism)
                val result = pool.parallelBfs(start, graph, blockSize)
                pool.close()
                return result
            }
        }
    }
}