package io.github.mnaeimaei.ranking;

import java.util.Comparator;

public final class Orderings {

    private Orderings() {
    }

    public static <K, V> Comparator<RankedEntry<K, V>> mostRecentFirst() {
        return (a, b) -> Long.compare(b.sequence(), a.sequence());
    }
}
