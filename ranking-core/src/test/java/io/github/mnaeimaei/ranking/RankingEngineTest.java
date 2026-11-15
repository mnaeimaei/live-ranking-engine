package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void mostRecentFirstBreaksTiesInFavourOfTheLatestAddedEntry() {
        RankingEngine<String, Integer> engine =
                new RankingEngine<>(HIGHEST_POINTS_FIRST.thenComparing(Orderings.mostRecentFirst()));

        engine.add("alice", 20);
        engine.add("bob", 20);
        engine.add("carol", 20);
        engine.add("dave", 5);

        assertEquals(List.of("carol", "bob", "alice", "dave"), keys(engine.ranking()));
    }

    @Test
    void rankingIsDeterministicWhenTheOrderingReportsATie() {
        RankingEngine<String, Integer> engine = new RankingEngine<>((a, b) -> 0);
        List<String> insertionOrder = List.of("zed", "amy", "kim", "bo", "lee", "ann", "max", "eve");

        insertionOrder.forEach(name -> engine.add(name, 1));

        assertEquals(insertionOrder, keys(engine.ranking()));
    }

    @Test
    void updateChangesTheValueAndReranks() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);
        engine.add("alice", 10);
        engine.add("bob", 12);

        Integer updated = engine.update("alice", points -> points + 5);

        assertEquals(15, updated);
        assertEquals(List.of("alice", "bob"), keys(engine.ranking()));
        assertEquals(15, engine.ranking().get(0).value());
    }

    @Test
    void updateKeepsTheOriginalInsertionSequence() {
        RankingEngine<String, Integer> engine =
                new RankingEngine<>(HIGHEST_POINTS_FIRST.thenComparing(Orderings.mostRecentFirst()));
        engine.add("alice", 10);
        engine.add("bob", 20);

        engine.update("alice", points -> 20);

        assertEquals(List.of("bob", "alice"), keys(engine.ranking()));
    }

    @Test
    void addingAnExistingKeyIsRejectedAndKeepsTheOriginalEntry() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);
        engine.add("alice", 10);

        EntryAlreadyExistsException thrown =
                assertThrows(EntryAlreadyExistsException.class, () -> engine.add("alice", 99));

        assertEquals("alice", thrown.key());
        assertEquals(1, engine.ranking().size());
        assertEquals(10, engine.ranking().get(0).value());
    }

    @Test
    void updatingAnUnknownKeyIsRejected() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);

        EntryNotFoundException thrown =
                assertThrows(EntryNotFoundException.class, () -> engine.update("ghost", points -> 1));

        assertEquals("ghost", thrown.key());
        assertTrue(engine.ranking().isEmpty());
    }

    @Test
    void removeReturnsTheFinalValueAndDropsTheEntry() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);
        engine.add("alice", 10);
        engine.add("bob", 20);

        Integer removed = engine.remove("alice");

        assertEquals(10, removed);
        assertEquals(List.of("bob"), keys(engine.ranking()));
    }

    @Test
    void removingAnUnknownKeyIsRejected() {
        RankingEngine<String, Integer> engine = new RankingEngine<>(HIGHEST_POINTS_FIRST);

        assertThrows(EntryNotFoundException.class, () -> engine.remove("ghost"));
    }

    @Test
    void aRemovedKeyCanBeAddedAgainAsTheMostRecentEntry() {
        RankingEngine<String, Integer> engine =
                new RankingEngine<>(HIGHEST_POINTS_FIRST.thenComparing(Orderings.mostRecentFirst()));
        engine.add("alice", 0);
        engine.add("bob", 0);

        engine.remove("alice");
        engine.add("alice", 0);

        assertEquals(List.of("alice", "bob"), keys(engine.ranking()));
    }

    private static List<String> keys(List<RankedEntry<String, Integer>> ranking) {
        return ranking.stream().map(RankedEntry::key).toList();
    }
}
