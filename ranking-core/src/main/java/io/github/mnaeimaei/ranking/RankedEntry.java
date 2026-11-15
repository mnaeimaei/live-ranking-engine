package io.github.mnaeimaei.ranking;

public record RankedEntry<K, V>(K key, V value, long sequence) {

    RankedEntry<K, V> withValue(V newValue) {
        return new RankedEntry<>(key, newValue, sequence);
    }
}
