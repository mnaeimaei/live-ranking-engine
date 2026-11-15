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

/**
 * A thread-safe, in-memory collection of live entries that is always available in ranked order.
 *
 * <p>The engine knows nothing about the domain it ranks. Callers supply:
 * <ul>
 *   <li>an <b>ordering</b> over {@link RankedEntry} &mdash; usually composed from {@link Orderings};</li>
 *   <li>optional {@link AdmissionRule}s that may veto new entries (e.g. "a team can only play one game at a time").</li>
 * </ul>
 * Whenever the ordering reports a tie, entries fall back to insertion order, so a ranking is always deterministic.
 *
 * <p><b>Concurrency.</b> Mutations take a write lock; admission rules and update functions run under that lock,
 * atomically with the change they guard. Reads take a read lock and return immutable snapshots.
 * Admission rules and update functions should therefore be fast and must not block.
 *
 * @param <K> the key that identifies an entry
 * @param <V> the ranked value; should be immutable
 */
public final class RankingEngine<K, V> {

    private final Comparator<RankedEntry<K, V>> ordering;
    private final List<AdmissionRule<K, V>> admissionRules;
    private final Map<K, RankedEntry<K, V>> entries = new HashMap<>();
    private final Collection<V> liveValues = new LiveValuesView();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long nextSequence = 0;

    /**
     * Creates an engine without admission rules.
     *
     * @param ordering how entries are ranked, best first
     */
    public RankingEngine(Comparator<? super RankedEntry<K, V>> ordering) {
        this(ordering, List.of());
    }

    private RankingEngine(Comparator<? super RankedEntry<K, V>> ordering, List<AdmissionRule<K, V>> admissionRules) {
        Objects.requireNonNull(ordering, "ordering");
        Comparator<RankedEntry<K, V>> callerOrdering = ordering::compare;
        this.ordering = callerOrdering.thenComparingLong(RankedEntry::sequence);
        this.admissionRules = List.copyOf(admissionRules);
    }

    /**
     * Starts building an engine that ranks entries with {@code ordering}, best first.
     *
     * @param ordering how entries are ranked
     * @param <K>      key type
     * @param <V>      value type
     * @return a builder for further configuration
     */
    public static <K, V> Builder<K, V> orderedBy(Comparator<? super RankedEntry<K, V>> ordering) {
        return new Builder<>(ordering);
    }

    /**
     * Adds a new entry. It becomes the most recent entry for {@link Orderings#mostRecentFirst()}.
     *
     * @param key   identifies the entry
     * @param value the initial value
     * @throws EntryAlreadyExistsException if {@code key} is already live
     * @throws EntryRejectedException      if an admission rule vetoes the entry
     */
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

    /**
     * Atomically replaces the value of a live entry with {@code change.apply(current)}.
     * The entry keeps its original insertion sequence.
     *
     * @param key    identifies the entry
     * @param change computes the new value from the current one; must not return {@code null}
     * @return the new value
     * @throws EntryNotFoundException if {@code key} is not live
     */
    public V update(K key, UnaryOperator<V> change) {
        Objects.requireNonNull(change, "change");
        return write(() -> {
            RankedEntry<K, V> current = requireEntry(key);
            V newValue = Objects.requireNonNull(change.apply(current.value()), "updated value");
            entries.put(key, current.withValue(newValue));
            return newValue;
        });
    }

    /**
     * Removes a live entry.
     *
     * @param key identifies the entry
     * @return the entry's final value
     * @throws EntryNotFoundException if {@code key} is not live
     */
    public V remove(K key) {
        return write(() -> {
            requireEntry(key);
            return entries.remove(key).value();
        });
    }

    /**
     * @return an immutable snapshot of all live entries, best first
     */
    public List<RankedEntry<K, V>> ranking() {
        return read(() -> sorted().toList());
    }

    /**
     * @param limit maximum number of entries to return; must not be negative
     * @return an immutable snapshot of the best {@code limit} entries
     */
    public List<RankedEntry<K, V>> top(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative: " + limit);
        }
        return read(() -> sorted().limit(limit).toList());
    }

    /**
     * @param key identifies the entry
     * @return the current value for {@code key}, or empty if it is not live
     */
    public Optional<V> find(K key) {
        return read(() -> Optional.ofNullable(entries.get(key)).map(RankedEntry::value));
    }

    /**
     * @return the number of live entries
     */
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

    /**
     * Fluent configuration for a {@link RankingEngine}.
     *
     * @param <K> key type
     * @param <V> value type
     */
    public static final class Builder<K, V> {

        private final Comparator<? super RankedEntry<K, V>> ordering;
        private final List<AdmissionRule<K, V>> admissionRules = new ArrayList<>();

        private Builder(Comparator<? super RankedEntry<K, V>> ordering) {
            this.ordering = Objects.requireNonNull(ordering, "ordering");
        }

        /**
         * Adds a rule that every new entry must pass. Rules run in the order they were added.
         *
         * @param rule the rule
         * @return this builder
         */
        public Builder<K, V> withAdmissionRule(AdmissionRule<K, V> rule) {
            admissionRules.add(Objects.requireNonNull(rule, "rule"));
            return this;
        }

        /**
         * @return a new, empty engine
         */
        public RankingEngine<K, V> build() {
            return new RankingEngine<>(ordering, admissionRules);
        }
    }
}
