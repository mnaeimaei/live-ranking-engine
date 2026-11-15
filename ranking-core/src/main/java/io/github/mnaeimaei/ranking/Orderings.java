package io.github.mnaeimaei.ranking;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;

/**
 * Reusable building blocks for ranking orders. Combine them with {@link Comparator#thenComparing(Comparator)}:
 *
 * <pre>{@code
 * Orderings.<Fixture, Game>highestFirst(game -> game.score().total())
 *          .thenComparing(Orderings.mostRecentFirst());
 * }</pre>
 */
public final class Orderings {

    private Orderings() {
    }

    /**
     * Ranks entries by a numeric score derived from their value, highest first.
     *
     * @param score extracts the score from a value
     * @param <K>   key type
     * @param <V>   value type
     * @return the comparator
     */
    public static <K, V> Comparator<RankedEntry<K, V>> highestFirst(ToDoubleFunction<? super V> score) {
        return (a, b) -> Double.compare(score.applyAsDouble(b.value()), score.applyAsDouble(a.value()));
    }

    /**
     * Ranks the most recently added entries first.
     *
     * @param <K> key type
     * @param <V> value type
     * @return the comparator
     */
    public static <K, V> Comparator<RankedEntry<K, V>> mostRecentFirst() {
        return (a, b) -> Long.compare(b.sequence(), a.sequence());
    }
}
