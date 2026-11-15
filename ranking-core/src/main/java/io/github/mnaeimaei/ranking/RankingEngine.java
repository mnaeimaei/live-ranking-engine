package io.github.mnaeimaei.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RankingEngine<K, V> {

    private final Comparator<? super RankedEntry<K, V>> ordering;
    private final Map<K, V> entries = new LinkedHashMap<>();

    public RankingEngine(Comparator<? super RankedEntry<K, V>> ordering) {
        this.ordering = ordering;
    }

    public void add(K key, V value) {
        entries.put(key, value);
    }

    public List<RankedEntry<K, V>> ranking() {
        List<RankedEntry<K, V>> ranking = new ArrayList<>();
        entries.forEach((key, value) -> ranking.add(new RankedEntry<>(key, value)));
        ranking.sort(ordering);
        return ranking;
    }
}
