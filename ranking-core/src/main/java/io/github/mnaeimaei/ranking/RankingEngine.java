package io.github.mnaeimaei.ranking;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RankingEngine<K, V> {

    private final Map<K, V> entries = new LinkedHashMap<>();

    public void add(K key, V value) {
        entries.put(key, value);
    }

    public List<RankedEntry<K, V>> ranking() {
        List<RankedEntry<K, V>> ranking = new ArrayList<>();
        entries.forEach((key, value) -> ranking.add(new RankedEntry<>(key, value)));
        return ranking;
    }
}
