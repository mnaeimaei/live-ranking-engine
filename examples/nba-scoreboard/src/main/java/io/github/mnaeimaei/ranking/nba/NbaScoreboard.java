package io.github.mnaeimaei.ranking.nba;

import io.github.mnaeimaei.ranking.Orderings;
import io.github.mnaeimaei.ranking.RankedEntry;
import io.github.mnaeimaei.ranking.RankingEngine;

import java.util.List;

public final class NbaScoreboard {

    private final RankingEngine<Fixture, Game> games = new RankingEngine<>(
            Orderings.<Fixture, Game>highestFirst(game -> game.score().total())
                    .thenComparing(Orderings.mostRecentFirst()));

    public void startGame(Team home, Team away) {
        Fixture fixture = new Fixture(home, away);
        games.add(fixture, new Game(fixture, Score.ZERO));
    }

    public Game updateScore(Team home, Team away, int homePoints, int awayPoints) {
        Score score = new Score(homePoints, awayPoints);
        return games.update(new Fixture(home, away), game -> new Game(game.fixture(), score));
    }

    public Game finishGame(Team home, Team away) {
        return games.remove(new Fixture(home, away));
    }

    public List<Game> summary() {
        return games.ranking().stream().map(RankedEntry::value).toList();
    }
}
