package io.github.mnaeimaei.ranking.nba;

import java.util.Objects;

/**
 * A live game: a fixture and its current score.
 *
 * @param fixture who plays whom
 * @param score   the current score
 */
public record Game(Fixture fixture, Score score) {

    /**
     * @throws NullPointerException if an argument is null
     */
    public Game {
        Objects.requireNonNull(fixture, "fixture");
        Objects.requireNonNull(score, "score");
    }

    /**
     * @return the game as a scoreboard line, for example {@code "Lakers 102 - Celtics 98"}
     */
    public String display() {
        return fixture.home().name() + " " + score.home() + " - " + fixture.away().name() + " " + score.away();
    }
}
