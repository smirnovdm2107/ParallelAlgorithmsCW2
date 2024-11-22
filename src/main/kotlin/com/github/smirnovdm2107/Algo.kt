package com.github.smirnovdm2107

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicIntegerArray

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

    abstract fun getNeigh(i: Int, j: Int): Int

    abstract fun getNeighCount(i: Int): Int

    abstract val size: Int

    abstract val edgeSize: Int
}

class ArrayGraph(
    private val graph: Array<Node>
) : Graph() {

    override val size: Int = graph.size

    override val edgeSize: Int = graph.sumOf { it.neighs.size } / 2

    override fun getNeigh(i: Int, j: Int): Int {
        return graph[i].neighs[j]
    }

    override fun getNeighCount(i: Int): Int {
        return graph[i].neighs.size
    }
}

private fun countNeighs(i: Int, nodesInEdge: Int): Int {
    if (i > 0 && i < nodesInEdge - 1) {
        return 2
    } else if (i > 0 || i < nodesInEdge - 1) {
        return 1
    }
    return 0
}

private fun createNodeN(i1: Int, i2: Int, i3: Int, nodesInEdge: Int): Int {
    return i1 + i2 * nodesInEdge + i3 * nodesInEdge * nodesInEdge
}

class CubeGraph(
    edgeLength: Int
) : Graph() {

    override val size: Int = (edgeLength + 1) * (edgeLength + 1) * (edgeLength + 1)

    override val edgeSize: Int = (size * 6 - 6 * (edgeLength + 1) * (edgeLength + 1)) / 2

    private val arr: IntArray = IntArray(size + edgeSize * 2)

    private val map: IntArray = IntArray(size)

    init {
        val nodesInEdge = edgeLength + 1
        var cur = 0
        for (i in 0 until size) {
            map[i] = cur
            val i1 = i % nodesInEdge
            val i2 = (i / nodesInEdge) % nodesInEdge
            val i3 = (i / nodesInEdge) / nodesInEdge
            arr[cur] = countNeighs(i1, nodesInEdge) + countNeighs(i2, nodesInEdge) + countNeighs(i3, nodesInEdge)
            cur++
            if (i1 > 0) {
                arr[cur] = createNodeN(i1 - 1, i2, i3, nodesInEdge)
                cur++
            }
            if (i1 < nodesInEdge - 1) {
                arr[cur] = createNodeN(i1 + 1, i2, i3, nodesInEdge)
                cur++
            }
            if (i2 > 0) {
                arr[cur] = createNodeN(i1, i2 - 1, i3, nodesInEdge)
                cur++
            }
            if (i2 < nodesInEdge - 1) {
                arr[cur] = createNodeN(i1, i2 + 1, i3, nodesInEdge)
                cur++
            }
            if (i3 > 0) {
                arr[cur] = createNodeN(i1, i2, i3 - 1, nodesInEdge)
                cur++
            }
            if (i3 < nodesInEdge - 1) {
                arr[cur] = createNodeN(i1, i2, i3 + 1, nodesInEdge)
                cur++
            }
        }
    }

    override fun getNeigh(i: Int, j: Int): Int {
        return arr[map[i] + j + 1]
    }

    override fun getNeighCount(i: Int): Int {
        return arr[map[i]]
    }
}


fun sequentialBfs(start: Int, graph: Graph, result: IntArray) {
    for (i in result.indices) {
        result[i] = -1
    }
    val q = ArrayDeque<Int>()
    q.add(start)
    result[start] = 0
    while (!q.isEmpty()) {
        val node = q.removeFirst()
        for (i in 0 until graph.getNeighCount(node)) {
            val neigh = graph.getNeigh(node, i)
            if (result[neigh] == -1) {
                result[neigh] = result[node] + 1
                q.add(neigh)
            }
        }
    }
}

fun ForkJoinPool.parallelBfs(
    start: Int,
    graph: Graph,
    result: IntArray
): IntArray {
    return parallelBfs(start, graph, AtomicIntegerArray(graph.size), result)
}

fun ForkJoinPool.parallelBfs(
    start: Int,
    graph: Graph,
    a: AtomicIntegerArray,
    result: IntArray
): IntArray {
    parallelFor(result.size) {
        result[it] = -1
    }
    a.set(start, 1)
    result[start] = 0
    var frontier = intArrayOf(start + 1)
    while (frontier.isNotEmpty()) {
        val degs = IntArray(frontier.size)
        parallelFor(frontier.size) {
            degs[it] = graph.getNeighCount(frontier[it] - 1)
        }
        val max = parallelScan(degs)
        val nextFront = IntArray(max)
        parallelFor(frontier.size) {
            val cur = frontier[it] - 1
            for (i in 0 until graph.getNeighCount(cur)) {
                val v = graph.getNeigh(cur, i)
                if (a.compareAndSet(v, 0, 1)) {
                    result[v] = result[cur] + 1
                    nextFront[degs[it] + i] = v + 1
                }
            }
        }
        val count = nextFront.count { it != 0 }
        frontier = IntArray(count)
        var cur = 0
        for (el in nextFront) {
            if (el != 0) {
                frontier[cur++] = el
            }
        }
    }
    return result
}

fun ForkJoinPool.parallelBfs2(
    start: Int,
    graph: Graph, // size n
    result: IntArray, // size n
    frontier: IntArray, // size n
    nextFrontier: IntArray, // size m
    sandbox: IntArray, // size m
    degs: IntArray
): IntArray {
    parallelFor(result.size) {
        result[it] = -1
    }
    val a = AtomicIntegerArray(graph.size)
    a.set(start, 1)
    result[start] = 0
    var frontierSize = 1
    frontier[0] = start + 1
    while (frontierSize != 0) {
        parallelFor(frontierSize) {
            degs[it] = graph.getNeighCount(frontier[it] - 1)
        }
        val max = parallelScan(degs, 0, frontierSize)
        parallelFor(frontierSize) {
            val cur = frontier[it] - 1
            for (i in 0 until graph.getNeighCount(cur)) {
                val v = graph.getNeigh(cur, i)
                if (a.compareAndSet(v, 0, 1)) {
                    result[v] = result[cur] + 1
                    nextFrontier[degs[it] + i] = v + 1
                }
            }
        }

        val nextSize = parallelFilter2(
            nextFrontier,
            0,
            max,
            sandbox,
            0,
            frontier,
            0
        ) {
            it != 0
        }
        frontierSize = nextSize
        parallelFor(max) {
            nextFrontier[it] = 0
        }
    }
    return result
}

private fun ForkJoinPool.parallelFilter2(
    arr: IntArray,
    l1: Int,
    r1: Int,
    sandbox: IntArray,
    l2: Int,
    result: IntArray,
    l3: Int,
    block: (Int) -> Boolean,
): Int {
    if (r1 - l1 == 0) {
        return 0
    }
    val realSize = r1 - l1
    parallelFor(realSize) {
        sandbox[l2 + it] = if (block(arr[l1 + it])) 1 else 0
    }
    val maxElem = parallelScan(sandbox, l2, l2 + realSize)
    if (maxElem == 0) {
        return 0
    }
    val last = block(arr[r1 - 1])
    parallelFor(realSize - 1) {
        if (sandbox[l2 + it + 1] == sandbox[l2 + it] + 1) {
            result[l3 + sandbox[l2 + it]] = arr[l1 + it]
        }
    }
    if (last) {
        result[l3 + maxElem - 1] = arr[r1 - 1]
    }
    return maxElem
}