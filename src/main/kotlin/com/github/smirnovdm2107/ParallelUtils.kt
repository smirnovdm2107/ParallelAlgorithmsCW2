package com.github.smirnovdm2107

import java.util.Collections
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.sqrt

fun ForkJoinPool.parallelFor(n: Int, blockSize: Int = sqrt(n.toDouble()).toInt(), block: (Int) -> Unit) {
    parallelFor(0, n, blockSize, block)
}

private fun ForkJoinPool.parallelFor(l: Int, r: Int, blockSize: Int = sqrt((r - l).toDouble()).toInt(), block: (Int) -> Unit) {
    if (r - l <= blockSize) {
        for (i in l until r) {
            block(i)
        }
        return
    }
    val m = (l + r) / 2
    val f1 = submit {
        parallelFor(l, m, blockSize, block)
    }
    val f2 = submit {
        parallelFor(m, r, blockSize, block)
    }
    f1.join()
    f2.join()
}

fun <T> ForkJoinPool.parallelReduce(arr: List<T>, blockSize: Int = sqrt(arr.size.toDouble()).toInt(), reduce: (T, T) -> T): T {
    return parallelReduce(arr, 0, arr.size, blockSize, reduce)
}

private fun <T> ForkJoinPool.parallelReduce(
    arr: List<T>,
    l: Int,
    r: Int,
    blockSize: Int = sqrt(arr.size.toDouble()).toInt(),
    reduce: (T, T) -> T
): T {
    if (r - l <= blockSize) {
        var res = arr[l]
        for (i in (l + 1) until r) {
            res = reduce(res, arr[i])
        }
        return res
    }
    val m = (l + r) / 2
    val f1 = submit<T> {
        parallelReduce(arr, l, m, blockSize, reduce)
    }
    val f2 = submit<T> {
        parallelReduce(arr, m, r, blockSize, reduce)
    }
    return reduce(f1.join(), f2.join())
}


fun ForkJoinPool.parallelMap(
    arr: IntArray,
    blockSize: Int = sqrt(arr.size.toDouble()).toInt(),
    mapper: (Int) -> Int,
): IntArray {
    val result = IntArray(arr.size)
    parallelMap(arr, 0, arr.size, mapper, result, blockSize)
    return result
}

private fun ForkJoinPool.parallelMap(
    arr1: IntArray,
    l: Int,
    r: Int,
    mapper: (Int) -> Int,
    arr2: IntArray,
    blockSize: Int = sqrt(arr1.size.toDouble()).toInt()
) {
    if (r - l <= blockSize) {
        for (i in l until r) {
            arr2[i] = mapper(arr1[i])
        }
        return
    }
    val m = (l + r) / 2
    val f1 = submit {
        parallelMap(arr1, l, m, mapper, arr2)
    }
    val f2 = submit {
        parallelMap(arr1, m, r, mapper, arr2)
    }
    f1.join()
    f2.join()
}

fun ForkJoinPool.parallelScan(
    arr: IntArray,
    blockSize: Int = sqrt(arr.size.toDouble()).toInt(),
    reduce: (Int, Int) -> Int
): Int {
    if (arr.isEmpty()) {
        return 0
    }
    upScan(arr, 0, arr.size, blockSize, reduce)
    val result = arr[arr.size - 1]
    arr[arr.size - 1] = 0
    downScan(arr, 0, arr.size, blockSize, reduce)
    return result
}

fun ForkJoinPool.upScan(
    arr: IntArray,
    l: Int,
    r: Int,
    blockSize: Int = sqrt(arr.size.toDouble()).toInt(),
    reduce: (Int, Int) -> Int
): Int {
    if (r - l == 1) {
        return arr[l]
    }
    val m = (l + r) / 2
    val left: Int
    val right: Int
    if (r - l < blockSize) {
        left = upScan(arr, l, m, blockSize, reduce)
        right = upScan(arr, m, r, blockSize, reduce)
    } else {
        val leftF = submit<Int> {
            upScan(arr, l, m, blockSize, reduce)
        }
        val rightF = submit<Int> {
            upScan(arr, m, r, blockSize, reduce)
        }
        left = leftF.join()
        right = rightF.join()
    }
    arr[r - 1] = reduce(left, right)
    return arr[r - 1]
}

fun ForkJoinPool.downScan(
    arr: IntArray,
    l: Int,
    r: Int,
    blockSize: Int = sqrt(arr.size.toDouble()).toInt(),
    reduce: (Int, Int) -> Int
) {
    if (r - l == 1) {
        return
    }
    val m = (l + r) / 2
    val tmp = arr[m - 1]
    arr[m - 1] = arr[r - 1]
    arr[r - 1] = reduce(arr[r - 1], tmp)
    if (r - l < blockSize) {
        downScan(arr, l, m, blockSize, reduce)
        downScan(arr, m, r, blockSize, reduce)
    } else {
        val leftF = submit {
            downScan(arr, l, m, blockSize, reduce)
        }
        val rightF = submit {
            downScan(arr, m, r, blockSize, reduce)
        }
        leftF.join()
        rightF.join()
    }
}

fun ForkJoinPool.parallelFilter(
    arr: IntArray,
    blockSize: Int = sqrt(arr.size.toDouble()).toInt(),
    block: (Int) -> Boolean,
): IntArray {
    if (arr.isEmpty()) {
        return arr
    }
    val prefix: IntArray = parallelMap(arr, blockSize) { if (block(it)) 1 else 0 }
    parallelScan(prefix, blockSize) { a, b -> a + b }
    val testLast = block(arr[arr.size - 1])
    val filteredSize = prefix[prefix.size - 1] + if (testLast) 1 else 0
    val result = IntArray(filteredSize)
    parallelFor(prefix.size - 1, blockSize) {
        if (prefix[it + 1] == prefix[it] + 1) {
            result[prefix[it]] = arr[it]
        }
    }
    if (testLast) {
        result[result.size - 1] = arr[arr.size - 1]
    }
    return result
}

