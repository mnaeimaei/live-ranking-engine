package io.github.mnaeimaei.ranking;

/**
 * An immutable snapshot of one live entry.
 *
 * @param key      identifies the entry
 * @param value    the ranked value at the time of the snapshot
 * @param sequence when the entry was added; higher means more recent. Unchanged by updates.
 * @param <K>      key type
 * @param <V>      value type
 */
public record RankedEntry<K, V>(K key, V value, long sequence) {

    RankedEntry<K, V> withValue(V newValue) {
        return new RankedEntry<>(key, newValue, sequence);
    }
}
