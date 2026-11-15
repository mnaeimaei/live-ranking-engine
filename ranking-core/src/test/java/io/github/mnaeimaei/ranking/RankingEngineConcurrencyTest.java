package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingEngineConcurrencyTest {

    private static final int THREADS = 8;
    private static final int OPERATIONS_PER_THREAD = 10_000;

    @Test
    void concurrentUpdatesToTheSameEntryAreNeverLost() throws Exception {
        RankingEngine<String, Integer> engine = new RankingEngine<>(Orderings.highestFirst(points -> points));
        engine.add("counter", 0);

        runConcurrently(thread -> {
            for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                engine.update("counter", points -> points + 1);
            }
        });

        assertEquals(THREADS * OPERATIONS_PER_THREAD, engine.find("counter").orElseThrow());
    }

    @Test
    void concurrentAddsKeepEveryEntryWithAUniqueSequence() throws Exception {
        RankingEngine<String, Integer> engine = new RankingEngine<>(Orderings.mostRecentFirst());

        runConcurrently(thread -> {
            for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                engine.add(thread + "-" + i, i);
            }
        });

        List<RankedEntry<String, Integer>> ranking = engine.ranking();
        assertEquals(THREADS * OPERATIONS_PER_THREAD, ranking.size());
        assertEquals(ranking.size(), ranking.stream().mapToLong(RankedEntry::sequence).distinct().count());
    }

    private interface Work {
        void run(int thread) throws Exception;
    }

    private static void runConcurrently(Work work) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                int thread = t;
                futures.add(pool.submit(() -> {
                    start.await();
                    work.run(thread);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
