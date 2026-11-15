package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingEngineTest {

    private static final Comparator<RankedEntry<String, Integer>> HIGHEST_POINTS_FIRST =
            (a, b) -> Integer.compare(b.value(), a.value());

    @Test
    void newEngineHasEmptyRanking() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);

        assertTrue(engine.ranking().isEmpty());
    }

    @Test
    void addedEntryAppearsInRanking() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);

        engine.add("alice", 10);

        List<RankedEntry<String, Integer>> ranking = engine.ranking();
        assertEquals(1, ranking.size());
        assertEquals("alice", ranking.get(0).key());
        assertEquals(10, ranking.get(0).value());
    }

    @Test
    void rankingFollowsTheGivenOrdering() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);

        engine.add("alice", 10);
        engine.add("bob", 30);
        engine.add("carol", 20);

        assertEquals(List.of("bob", "carol", "alice"), keys(engine.ranking()));
    }

    private static List<String> keys(List<RankedEntry<String, Integer>> ranking) {
        return ranking.stream().map(RankedEntry::key).toList();
    }
}
