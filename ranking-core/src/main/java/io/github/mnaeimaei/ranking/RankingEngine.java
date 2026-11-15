package io.github.mnaeimaei.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RankingEngine<K, V> {

    private final Comparator<RankedEntry<K, V>> ordering;
    private final Map<K, RankedEntry<K, V>> entries = new HashMap<>();
    private long nextSequence = 0;

    public RankingEngine(Comparator<? super RankedEntry<K, V>> ordering) {
        Comparator<RankedEntry<K, V>> callerOrdering = ordering::compare;
        this.ordering = callerOrdering.thenComparingLong(RankedEntry::sequence);
    }

    public void add(K key, V value) {
        entries.put(key, new RankedEntry<>(key, value, nextSequence++));
    }

    public List<RankedEntry<K, V>> ranking() {
        List<RankedEntry<K, V>> ranking = new ArrayList<>(entries.values());
        ranking.sort(ordering);
        return ranking;
    }
}
