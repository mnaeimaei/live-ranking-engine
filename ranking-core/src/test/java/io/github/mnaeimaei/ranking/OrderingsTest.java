package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderingsTest {

    private record Run(String name, double accuracy) {
    }

    @Test
    void highestFirstSortsByADerivedScoreDescending() {
        List<RankedEntry<String, Run>> entries = new ArrayList<>(List.of(
                new RankedEntry<>("a", new Run("a", 0.71), 0),
                new RankedEntry<>("b", new Run("b", 0.93), 1),
                new RankedEntry<>("c", new Run("c", 0.88), 2)));

        entries.sort(Orderings.highestFirst(Run::accuracy));

        assertEquals(List.of("b", "c", "a"), entries.stream().map(RankedEntry::key).toList());
    }

    @Test
    void mostRecentFirstPutsHigherSequencesFirst() {
        List<RankedEntry<String, Integer>> entries = new ArrayList<>(List.of(
                new RankedEntry<>("old", 1, 0),
                new RankedEntry<>("new", 1, 7)));

        entries.sort(Orderings.mostRecentFirst());

        assertEquals("new", entries.get(0).key());
    }
}
