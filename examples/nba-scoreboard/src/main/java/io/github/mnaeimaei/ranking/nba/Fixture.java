package io.github.mnaeimaei.ranking.nba;

import java.util.Objects;

/**
 * Who plays whom. A fixture identifies a live game on the scoreboard.
 *
 * @param home the home team
 * @param away the away team
 */
public record Fixture(Team home, Team away) {

    /**
     * @throws NullPointerException     if either team is null
     * @throws IllegalArgumentException if both sides are the same team
     */
    public Fixture {
        Objects.requireNonNull(home, "home");
        Objects.requireNonNull(away, "away");
        if (home.equals(away)) {
            throw new IllegalArgumentException("a team cannot play itself: " + home.name());
        }
    }

    /**
     * @param team a team
     * @return whether {@code team} plays in this fixture
     */
    public boolean involves(Team team) {
        return home.equals(team) || away.equals(team);
    }
}
