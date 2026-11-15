package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingEngineTest {

    private static final Comparator<RankedEntry<String, Integer>> HIGHEST_POINTS_FIRST =
            Orderings.highestFirst(points -> points);

    @Test
    void newEngineHasEmptyRanking() {
        RankingEngine<String, Integer> engine = pointsEngine();

        assertTrue(engine.ranking().isEmpty());
    }

    @Test
    void addedEntryAppearsInRanking() {
        RankingEngine<String, Integer> engine = pointsEngine();

        engine.add("alice", 10);

        List<RankedEntry<String, Integer>> ranking = engine.ranking();
        assertEquals(1, ranking.size());
        assertEquals("alice", ranking.get(0).key());
        assertEquals(10, ranking.get(0).value());
    }

    @Test
    void rankingFollowsTheGivenOrdering() {
        RankingEngine<String, Integer> engine = pointsEngine();

        engine.add("alice", 10);
        engine.add("bob", 30);
        engine.add("carol", 20);

        assertEquals(List.of("bob", "carol", "alice"), keys(engine.ranking()));
    }

    @Test
    void mostRecentFirstBreaksTiesInFavourOfTheLatestAddedEntry() {
        RankingEngine<String, Integer> engine = pointsThenRecencyEngine();

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
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);
        engine.add("bob", 12);

        Integer updated = engine.update("alice", points -> points + 5);

        assertEquals(15, updated);
        assertEquals(List.of("alice", "bob"), keys(engine.ranking()));
        assertEquals(15, engine.ranking().get(0).value());
    }

    @Test
    void updateKeepsTheOriginalInsertionSequence() {
        RankingEngine<String, Integer> engine = pointsThenRecencyEngine();
        engine.add("alice", 10);
        engine.add("bob", 20);

        engine.update("alice", points -> 20);

        assertEquals(List.of("bob", "alice"), keys(engine.ranking()));
    }

    @Test
    void addingAnExistingKeyIsRejectedAndKeepsTheOriginalEntry() {
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);

        EntryAlreadyExistsException thrown =
                assertThrows(EntryAlreadyExistsException.class, () -> engine.add("alice", 99));

        assertEquals("alice", thrown.key());
        assertEquals(1, engine.ranking().size());
        assertEquals(10, engine.ranking().get(0).value());
    }

    @Test
    void updatingAnUnknownKeyIsRejected() {
        RankingEngine<String, Integer> engine = pointsEngine();

        EntryNotFoundException thrown =
                assertThrows(EntryNotFoundException.class, () -> engine.update("ghost", points -> 1));

        assertEquals("ghost", thrown.key());
        assertTrue(engine.ranking().isEmpty());
    }

    @Test
    void removeReturnsTheFinalValueAndDropsTheEntry() {
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);
        engine.add("bob", 20);

        Integer removed = engine.remove("alice");

        assertEquals(10, removed);
        assertEquals(List.of("bob"), keys(engine.ranking()));
    }

    @Test
    void removingAnUnknownKeyIsRejected() {
        RankingEngine<String, Integer> engine = pointsEngine();

        assertThrows(EntryNotFoundException.class, () -> engine.remove("ghost"));
    }

    @Test
    void aRemovedKeyCanBeAddedAgainAsTheMostRecentEntry() {
        RankingEngine<String, Integer> engine = pointsThenRecencyEngine();
        engine.add("alice", 0);
        engine.add("bob", 0);

        engine.remove("alice");
        engine.add("alice", 0);

        assertEquals(List.of("alice", "bob"), keys(engine.ranking()));
    }

    @Test
    void rankingIsAnUnmodifiableSnapshot() {
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);

        List<RankedEntry<String, Integer>> snapshot = engine.ranking();
        engine.update("alice", points -> 99);
        engine.add("bob", 50);

        assertEquals(List.of(new RankedEntry<>("alice", 10, 0)), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }

    @Test
    void topReturnsAtMostTheRequestedNumberOfLeaders() {
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);
        engine.add("bob", 30);
        engine.add("carol", 20);

        assertEquals(List.of("bob", "carol"), keys(engine.top(2)));
        assertEquals(3, engine.top(10).size());
        assertTrue(engine.top(0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> engine.top(-1));
    }

    @Test
    void findLooksUpTheCurrentValueOfAKey() {
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);

        assertEquals(Optional.of(10), engine.find("alice"));
        assertEquals(Optional.empty(), engine.find("ghost"));
        assertEquals(1, engine.size());
    }

    @Test
    void nullsAreRejectedAtTheBoundary() {
        RankingEngine<String, Integer> engine = pointsEngine();
        engine.add("alice", 10);

        assertThrows(NullPointerException.class, () -> new RankingEngine<String, Integer>(null));
        assertThrows(NullPointerException.class, () -> engine.add(null, 1));
        assertThrows(NullPointerException.class, () -> engine.add("bob", null));
        assertThrows(NullPointerException.class, () -> engine.update("alice", null));
        assertThrows(NullPointerException.class, () -> engine.update("alice", points -> null));
        assertEquals(Optional.of(10), engine.find("alice"));
    }

    @Test
    void admissionRulesCanRejectNewEntries() {
        AdmissionRule<String, Integer> atMostTwoEntries = (key, value, live) -> {
            if (live.size() >= 2) {
                throw new EntryRejectedException("leaderboard is full, cannot admit " + key);
            }
        };
        RankingEngine<String, Integer> engine = RankingEngine.orderedBy(HIGHEST_POINTS_FIRST)
                .withAdmissionRule(atMostTwoEntries)
                .build();
        engine.add("alice", 10);
        engine.add("bob", 20);

        EntryRejectedException thrown =
                assertThrows(EntryRejectedException.class, () -> engine.add("carol", 30));

        assertEquals("leaderboard is full, cannot admit carol", thrown.getMessage());
        assertEquals(List.of("bob", "alice"), keys(engine.ranking()));
    }

    @Test
    void admissionRulesSeeTheValuesThatAreCurrentlyLive() {
        AdmissionRule<String, Integer> uniquePoints = (key, value, live) -> {
            if (live.contains(value)) {
                throw new EntryRejectedException("points already taken: " + value);
            }
        };
        RankingEngine<String, Integer> engine = RankingEngine.orderedBy(HIGHEST_POINTS_FIRST)
                .withAdmissionRule(uniquePoints)
                .build();
        engine.add("alice", 10);

        assertThrows(EntryRejectedException.class, () -> engine.add("bob", 10));
        engine.remove("alice");
        engine.add("bob", 10);

        assertEquals(List.of("bob"), keys(engine.ranking()));
    }

    private static RankingEngine<String, Integer> pointsEngine() {
        return new RankingEngine<>(HIGHEST_POINTS_FIRST);
    }

    private static RankingEngine<String, Integer> pointsThenRecencyEngine() {
        return new RankingEngine<>(HIGHEST_POINTS_FIRST.thenComparing(Orderings.mostRecentFirst()));
    }

    private static List<String> keys(List<RankedEntry<String, Integer>> ranking) {
        return ranking.stream().map(RankedEntry::key).toList();
    }
}
