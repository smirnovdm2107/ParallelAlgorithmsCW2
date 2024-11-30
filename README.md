
Comparasion of 3 bfs algorithms (cube graph with edge length - 500):
1) ParallelBfs - parallel bfs with 4 threads with allocations 
2) ParallelBfs - parallel bfs with 4 threads without allocations
3) SequentialQuickSort - sequential bfs

| Benchmark     | Mode                    | Cnt  | Score | Error | Units|
|---------------|-------------------------|------|-------|-------|------|
 | ParallelBfs2  | avgt                    | 5    |  17478.455 ± |   1643.879 | ms/op |
| ParallelBfs   | avgt |    5 |     18561.939 ± |  1355.934 |  ms/op |
| SequentialBfs |  avgt |    5 |    40108.512 ± |    3081.815 |  ms/op |

Performance scale ~ 2.29 

To start benchmark test use `./gradlew jmh` (java 21, 8g RAM)
