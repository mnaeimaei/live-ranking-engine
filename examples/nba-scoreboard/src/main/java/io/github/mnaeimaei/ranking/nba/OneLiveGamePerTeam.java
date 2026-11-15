package io.github.mnaeimaei.ranking.nba;

import io.github.mnaeimaei.ranking.AdmissionRule;

import java.util.Collection;
import java.util.List;

/** Rejects a new game if either of its teams is already playing. */
final class OneLiveGamePerTeam implements AdmissionRule<Fixture, Game> {

    @Override
    public void check(Fixture fixture, Game candidate, Collection<Game> live) {
        for (Team team : List.of(fixture.home(), fixture.away())) {
            if (live.stream().anyMatch(game -> game.fixture().involves(team))) {
                throw new TeamAlreadyPlayingException(team);
            }
        }
    }
}
