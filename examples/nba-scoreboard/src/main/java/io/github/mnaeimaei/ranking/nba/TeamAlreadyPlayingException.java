package io.github.mnaeimaei.ranking.nba;

import io.github.mnaeimaei.ranking.EntryRejectedException;

/** Thrown when a game is started for a team that is already playing a live game. */
public final class TeamAlreadyPlayingException extends EntryRejectedException {

    private static final long serialVersionUID = 1L;

    private final transient Team team;

    /**
     * @param team the team that is already playing
     */
    public TeamAlreadyPlayingException(Team team) {
        super(team.name() + " is already playing a live game");
        this.team = team;
    }

    /**
     * @return the team that is already playing
     */
    public Team team() {
        return team;
    }
}
