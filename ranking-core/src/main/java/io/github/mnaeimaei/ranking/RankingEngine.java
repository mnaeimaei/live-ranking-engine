package io.github.mnaeimaei.ranking;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class RankingEngine<K, V> {

    private final Comparator<RankedEntry<K, V>> ordering;
    private final List<AdmissionRule<K, V>> admissionRules;
    private final Map<K, RankedEntry<K, V>> entries = new HashMap<>();
    private final Collection<V> liveValues = new LiveValuesView();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
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
        write(() -> {
            if (entries.containsKey(key)) {
                throw new EntryAlreadyExistsException(key);
            }
            for (AdmissionRule<K, V> rule : admissionRules) {
                rule.check(key, value, liveValues);
            }
            return entries.put(key, new RankedEntry<>(key, value, nextSequence++));
        });
    }

    public V update(K key, UnaryOperator<V> change) {
        Objects.requireNonNull(change, "change");
        return write(() -> {
            RankedEntry<K, V> current = requireEntry(key);
            V newValue = Objects.requireNonNull(change.apply(current.value()), "updated value");
            entries.put(key, current.withValue(newValue));
            return newValue;
        });
    }

    public V remove(K key) {
        return write(() -> {
            requireEntry(key);
            return entries.remove(key).value();
        });
    }

    public List<RankedEntry<K, V>> ranking() {
        return read(() -> sorted().toList());
    }

    public List<RankedEntry<K, V>> top(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative: " + limit);
        }
        return read(() -> sorted().limit(limit).toList());
    }

    public Optional<V> find(K key) {
        return read(() -> Optional.ofNullable(entries.get(key)).map(RankedEntry::value));
    }

    public int size() {
        return read(entries::size);
    }

    private <T> T read(Supplier<T> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private <T> T write(Supplier<T> action) {
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
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

    private final class LiveValuesView extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            Iterator<RankedEntry<K, V>> delegate = entries.values().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public V next() {
                    return delegate.next().value();
                }
            };
        }

        @Override
        public int size() {
            return entries.size();
        }
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
