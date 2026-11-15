package io.github.mnaeimaei.ranking.nba;

import java.util.Objects;

/**
 * An NBA team, identified by its trimmed name.
 *
 * @param name the team name, for example {@code "Lakers"}
 */
public record Team(String name) {

    /**
     * @throws NullPointerException     if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public Team {
        name = Objects.requireNonNull(name, "name").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("team name must not be blank");
        }
    }
}
