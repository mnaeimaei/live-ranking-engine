package io.github.mnaeimaei.ranking.nba;

/**
 * The points of both sides of a game.
 *
 * @param home points scored by the home team
 * @param away points scored by the away team
 */
public record Score(int home, int away) {

    /** The score of a game that has just started. */
    public static final Score ZERO = new Score(0, 0);

    /**
     * @throws IllegalArgumentException if either side is negative
     */
    public Score {
        if (home < 0 || away < 0) {
            throw new IllegalArgumentException("points must not be negative: " + home + "-" + away);
        }
    }

    /**
     * @return combined points of both teams
     */
    public int total() {
        return home + away;
    }
}
