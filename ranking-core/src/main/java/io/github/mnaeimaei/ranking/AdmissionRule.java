package io.github.mnaeimaei.ranking;

import java.util.Collection;

/**
 * A domain rule that decides whether a new entry may join the ranking.
 * Rules run under the engine's write lock, atomically with the add they guard.
 *
 * @param <K> key type
 * @param <V> value type
 */
@FunctionalInterface
public interface AdmissionRule<K, V> {

    /**
     * Checks a candidate entry.
     *
     * @param key       the key being added
     * @param candidate the value being added
     * @param live      read-only view of the values currently live; valid only during this call
     * @throws EntryRejectedException (or a subclass) to veto the entry
     */
    void check(K key, V candidate, Collection<V> live);
}
