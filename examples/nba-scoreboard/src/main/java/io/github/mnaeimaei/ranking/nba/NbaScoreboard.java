package io.github.mnaeimaei.ranking.nba;

import io.github.mnaeimaei.ranking.Orderings;
import io.github.mnaeimaei.ranking.RankedEntry;
import io.github.mnaeimaei.ranking.RankingEngine;

import java.util.Comparator;
import java.util.List;

/**
 * A live NBA scoreboard: the reference use case of {@code ranking-core}.
 *
 * <p>Games are ranked by total points, and games with the same total by the most recently started first.
 * A team can only play one live game at a time. All operations are thread-safe.
 *
 * <p>Failures are reported with exceptions rather than ignored:
 * {@link io.github.mnaeimaei.ranking.EntryAlreadyExistsException} for a game that is already live,
 * {@link io.github.mnaeimaei.ranking.EntryNotFoundException} for a game that is not,
 * {@link TeamAlreadyPlayingException} for a busy team and {@link IllegalArgumentException} for invalid input.
 */
public final class NbaScoreboard {

    private static final Comparator<RankedEntry<Fixture, Game>> HIGHEST_TOTAL_THEN_MOST_RECENT =
            Orderings.<Fixture, Game>highestFirst(game -> game.score().total())
                    .thenComparing(Orderings.mostRecentFirst());

    private final RankingEngine<Fixture, Game> games = RankingEngine
            .orderedBy(HIGHEST_TOTAL_THEN_MOST_RECENT)
            .withAdmissionRule(new OneLiveGamePerTeam())
            .build();

    /**
     * Starts a game at 0-0.
     *
     * @param home the home team
     * @param away the away team
     * @return the new game
     */
    public Game startGame(Team home, Team away) {
        Fixture fixture = new Fixture(home, away);
        Game game = new Game(fixture, Score.ZERO);
        games.add(fixture, game);
        return game;
    }

    /**
     * Replaces the score of a live game.
     *
     * @param home       the home team
     * @param away       the away team
     * @param homePoints the home team's points
     * @param awayPoints the away team's points
     * @return the updated game
     */
    public Game updateScore(Team home, Team away, int homePoints, int awayPoints) {
        Score score = new Score(homePoints, awayPoints);
        return games.update(new Fixture(home, away), game -> new Game(game.fixture(), score));
    }

    /**
     * Finishes a live game and removes it from the scoreboard.
     *
     * @param home the home team
     * @param away the away team
     * @return the game with its final score
     */
    public Game finishGame(Team home, Team away) {
        return games.remove(new Fixture(home, away));
    }

    /**
     * @return the live games in ranked order, as an immutable snapshot
     */
    public List<Game> summary() {
        return games.ranking().stream().map(RankedEntry::value).toList();
    }
}
