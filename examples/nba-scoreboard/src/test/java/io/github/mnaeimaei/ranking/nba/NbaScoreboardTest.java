package io.github.mnaeimaei.ranking.nba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NbaScoreboardTest {

    private static final Team LAKERS = new Team("Lakers");
    private static final Team CELTICS = new Team("Celtics");

    private NbaScoreboard scoreboard;

    @BeforeEach
    void setUp() {
        scoreboard = new NbaScoreboard();
    }

    @Test
    void aNewScoreboardHasNoGames() {
        assertTrue(scoreboard.summary().isEmpty());
    }

    @Test
    void aStartedGameAppearsAtZeroZero() {
        scoreboard.startGame(LAKERS, CELTICS);

        assertEquals(List.of("Lakers 0 - Celtics 0"), lines());
    }

    private List<String> lines() {
        return scoreboard.summary().stream().map(Game::display).toList();
    }
}
