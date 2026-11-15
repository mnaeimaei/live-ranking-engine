package io.github.mnaeimaei.ranking;

import java.util.Collection;

@FunctionalInterface
public interface AdmissionRule<K, V> {

    void check(K key, V candidate, Collection<V> live);
}
