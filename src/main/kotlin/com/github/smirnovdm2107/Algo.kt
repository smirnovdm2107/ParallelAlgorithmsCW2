package com.github.smirnovdm2107

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.atomic.AtomicLong

data class Node(
    val neighs: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        return neighs.contentEquals(other.neighs)
    }

    override fun hashCode(): Int {
        return neighs.contentHashCode()
    }
}

abstract class Graph {

    abstract operator fun get(i: Int): Node

    abstract val size: Int
}

class ArrayGraph(
    private val graph: Array<Node>
) : Graph() {

    override val size: Int = graph.size

    override fun get(i: Int): Node {
        return graph[i]
    }
}

class CubeGraph(
    private val edgeSize: Int
) : Graph() {

    override val size: Int = edgeSize * edgeSize * edgeSize

    override fun get(i: Int): Node {
        val i1 = i % edgeSize
        val i2 = (i / edgeSize) % edgeSize
        val i3 = (i / edgeSize) / edgeSize
        return Node(
            listOf(
                Triple(i1, i2, i3 + 1),
                Triple(i1, i2, i3 - 1),
                Triple(i1, i2 + 1, i3),
                Triple(i1, i2 - 1, i3),
                Triple(i1 + 1, i2, i3),
                Triple(i1 - 1, i2, i3)
            ).filter { (i1, i2, i3) ->
                i1 in 0 until edgeSize &&
                        i2 in 0 until edgeSize &&
                        i3 in 0 until edgeSize
            }.map { (i1, i2, i3) ->
                i1 + i2 * edgeSize + i3 * edgeSize * edgeSize
            }.toIntArray()
        )
    }
}


fun sequentialBfs(start: Int, graph: Graph): IntArray {
    val result = IntArray(graph.size) { -1 }
    val q = ArrayDeque<Int>()
    q.add(start)
    result[start] = 0
    while (!q.isEmpty()) {
        val node = q.removeFirst()
        for (neigh in graph[node].neighs) {
            if (result[neigh] == -1) {
                result[neigh] = result[node] + 1
                q.add(neigh)
            }
        }
    }
    return result
}

fun ForkJoinPool.parallelBfs(start: Int, graph: Graph, blockSize: Int): IntArray {
    val result = IntArray(graph.size)
    parallelFor(result.size) {
        result[it] = -1
    }
    val a = AtomicIntegerArray(graph.size)
    a.set(start, 1)
    result[start] = 0
    var frontier = intArrayOf(start + 1)
    while (frontier.isNotEmpty()) {
        val degs = IntArray(frontier.size)
        parallelFor(frontier.size) {
            degs[it] = graph[frontier[it] - 1].neighs.size
        }
        val max = parallelScan(degs) { i1, i2 -> i1 + i2 }
        val nextFront = IntArray(max + degs.last())
        parallelFor(frontier.size) {
            val cur = frontier[it] - 1
            for (i in 0 until graph[cur].neighs.size) {
                val v = graph[cur].neighs[i]
                if (a.compareAndSet(v, 0, 1)) {
                    result[v] = result[cur] + 1
                    nextFront[degs[it] + i] = v + 1
                }
            }
        }
        frontier = parallelFilter(nextFront) {
            it != 0
        }
    }
    return result
}