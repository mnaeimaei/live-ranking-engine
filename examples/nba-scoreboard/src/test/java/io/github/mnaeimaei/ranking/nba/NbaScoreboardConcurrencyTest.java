package io.github.mnaeimaei.ranking.nba;

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

class NbaScoreboardConcurrencyTest {

    private static final int OPPONENTS = 16;

    @Test
    void whenManyGamesForOneTeamStartAtOnceExactlyOneIsAdmitted() throws Exception {
        NbaScoreboard scoreboard = new NbaScoreboard();
        Team lakers = new Team("Lakers");
        ExecutorService pool = Executors.newFixedThreadPool(OPPONENTS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> attempts = new ArrayList<>();

        try {
            for (int i = 0; i < OPPONENTS; i++) {
                Team opponent = new Team("Opponent " + i);
                attempts.add(pool.submit(() -> {
                    start.await();
                    try {
                        scoreboard.startGame(lakers, opponent);
                        return true;
                    } catch (TeamAlreadyPlayingException rejected) {
                        return false;
                    }
                }));
            }
            start.countDown();

            int admitted = 0;
            for (Future<Boolean> attempt : attempts) {
                if (attempt.get(10, TimeUnit.SECONDS)) {
                    admitted++;
                }
            }

            assertEquals(1, admitted);
            assertEquals(1, scoreboard.summary().size());
        } finally {
            pool.shutdownNow();
        }
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
