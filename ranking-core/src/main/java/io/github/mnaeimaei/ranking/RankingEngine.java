package io.github.mnaeimaei.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class RankingEngine<K, V> {

    private final Comparator<RankedEntry<K, V>> ordering;
    private final Map<K, RankedEntry<K, V>> entries = new HashMap<>();
    private long nextSequence = 0;

    public RankingEngine(Comparator<? super RankedEntry<K, V>> ordering) {
        Objects.requireNonNull(ordering, "ordering");
        Comparator<RankedEntry<K, V>> callerOrdering = ordering::compare;
        this.ordering = callerOrdering.thenComparingLong(RankedEntry::sequence);
    }

    public void add(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        if (entries.containsKey(key)) {
            throw new EntryAlreadyExistsException(key);
        }
        entries.put(key, new RankedEntry<>(key, value, nextSequence++));
    }

    public V update(K key, UnaryOperator<V> change) {
        Objects.requireNonNull(change, "change");
        RankedEntry<K, V> current = requireEntry(key);
        V newValue = Objects.requireNonNull(change.apply(current.value()), "updated value");
        RankedEntry<K, V> updated = current.withValue(newValue);
        entries.put(key, updated);
        return updated.value();
    }

    public V remove(K key) {
        requireEntry(key);
        return entries.remove(key).value();
    }

    public List<RankedEntry<K, V>> ranking() {
        return sorted().toList();
    }

    public List<RankedEntry<K, V>> top(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative: " + limit);
        }
        return sorted().limit(limit).toList();
    }

    public Optional<V> find(K key) {
        return Optional.ofNullable(entries.get(key)).map(RankedEntry::value);
    }

    public int size() {
        return entries.size();
    }

    private Stream<RankedEntry<K, V>> sorted() {
        return entries.values().stream().sorted(ordering);
    }

    private RankedEntry<K, V> requireEntry(K key) {
        RankedEntry<K, V> entry = entries.get(key);
        if (entry == null) {
            throw new EntryNotFoundException(key);
        }
        return entry;
    }
}
