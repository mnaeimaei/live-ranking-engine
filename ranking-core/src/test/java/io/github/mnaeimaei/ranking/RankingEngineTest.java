package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingEngineTest {

    @Test
    void newEngineHasEmptyRanking() {
        RankingEngine<String, Integer> engine = new RankingEngine<>();

        assertTrue(engine.ranking().isEmpty());
    }

    @Test
    void addedEntryAppearsInRanking() {
        RankingEngine<String, Integer> engine = new RankingEngine<>();

        engine.add("alice", 10);

        List<RankedEntry<String, Integer>> ranking = engine.ranking();
        assertEquals(1, ranking.size());
        assertEquals("alice", ranking.get(0).key());
        assertEquals(10, ranking.get(0).value());
    }
}
