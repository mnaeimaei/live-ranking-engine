package io.github.mnaeimaei.ranking;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;

public final class Orderings {

    private Orderings() {
    }

    public static <K, V> Comparator<RankedEntry<K, V>> highestFirst(ToDoubleFunction<? super V> score) {
        return (a, b) -> Double.compare(score.applyAsDouble(b.value()), score.applyAsDouble(a.value()));
    }

    public static <K, V> Comparator<RankedEntry<K, V>> mostRecentFirst() {
        return (a, b) -> Long.compare(b.sequence(), a.sequence());
    }
}
