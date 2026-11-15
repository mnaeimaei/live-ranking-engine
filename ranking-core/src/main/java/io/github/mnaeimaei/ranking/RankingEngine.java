package io.github.mnaeimaei.ranking;

import java.util.ArrayList;
import java.util.Collection;
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
    private final List<AdmissionRule<K, V>> admissionRules;
    private final Map<K, RankedEntry<K, V>> entries = new HashMap<>();
    private long nextSequence = 0;

    public RankingEngine(Comparator<? super RankedEntry<K, V>> ordering) {
        this(ordering, List.of());
    }

    private RankingEngine(Comparator<? super RankedEntry<K, V>> ordering, List<AdmissionRule<K, V>> admissionRules) {
        Objects.requireNonNull(ordering, "ordering");
        Comparator<RankedEntry<K, V>> callerOrdering = ordering::compare;
        this.ordering = callerOrdering.thenComparingLong(RankedEntry::sequence);
        this.admissionRules = List.copyOf(admissionRules);
    }

    public static <K, V> Builder<K, V> orderedBy(Comparator<? super RankedEntry<K, V>> ordering) {
        return new Builder<>(ordering);
    }

    public void add(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        if (entries.containsKey(key)) {
            throw new EntryAlreadyExistsException(key);
        }
        Collection<V> live = entries.values().stream().map(RankedEntry::value).toList();
        for (AdmissionRule<K, V> rule : admissionRules) {
            rule.check(key, value, live);
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

    public static final class Builder<K, V> {

        private final Comparator<? super RankedEntry<K, V>> ordering;
        private final List<AdmissionRule<K, V>> admissionRules = new ArrayList<>();

        private Builder(Comparator<? super RankedEntry<K, V>> ordering) {
            this.ordering = Objects.requireNonNull(ordering, "ordering");
        }

        public Builder<K, V> withAdmissionRule(AdmissionRule<K, V> rule) {
            admissionRules.add(Objects.requireNonNull(rule, "rule"));
            return this;
        }

        public RankingEngine<K, V> build() {
            return new RankingEngine<>(ordering, admissionRules);
        }
    }
}
