package io.github.mnaeimaei.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class RankingEngine<K, V> {

    private final Comparator<RankedEntry<K, V>> ordering;
    private final Map<K, RankedEntry<K, V>> entries = new HashMap<>();
    private long nextSequence = 0;

    public RankingEngine(Comparator<? super RankedEntry<K, V>> ordering) {
        Comparator<RankedEntry<K, V>> callerOrdering = ordering::compare;
        this.ordering = callerOrdering.thenComparingLong(RankedEntry::sequence);
    }

    public void add(K key, V value) {
        if (entries.containsKey(key)) {
            throw new EntryAlreadyExistsException(key);
        }
        entries.put(key, new RankedEntry<>(key, value, nextSequence++));
    }

    public V update(K key, UnaryOperator<V> change) {
        RankedEntry<K, V> current = requireEntry(key);
        RankedEntry<K, V> updated = current.withValue(change.apply(current.value()));
        entries.put(key, updated);
        return updated.value();
    }

    public V remove(K key) {
        requireEntry(key);
        return entries.remove(key).value();
    }

    public List<RankedEntry<K, V>> ranking() {
        return entries.values().stream().sorted(ordering).toList();
    }

    public List<RankedEntry<K, V>> top(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative: " + limit);
        }
        return entries.values().stream().sorted(ordering).limit(limit).toList();
    }

    public Optional<V> find(K key) {
        return Optional.ofNullable(entries.get(key)).map(RankedEntry::value);
    }

    public int size() {
        return entries.size();
    }

    private RankedEntry<K, V> requireEntry(K key) {
        RankedEntry<K, V> entry = entries.get(key);
        if (entry == null) {
            throw new EntryNotFoundException(key);
        }
        return entry;
    }
}
