package io.github.mnaeimaei.ranking.nba;

import io.github.mnaeimaei.ranking.RankedEntry;
import io.github.mnaeimaei.ranking.RankingEngine;

import java.util.List;

public final class NbaScoreboard {

    private final RankingEngine<Fixture, Game> games = new RankingEngine<>((a, b) -> 0);

    public void startGame(Team home, Team away) {
        Fixture fixture = new Fixture(home, away);
        games.add(fixture, new Game(fixture, Score.ZERO));
    }

    public List<Game> summary() {
        return games.ranking().stream().map(RankedEntry::value).toList();
    }
}
