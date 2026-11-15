package io.github.mnaeimaei.ranking.nba;

import io.github.mnaeimaei.ranking.EntryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void updatingAScoreReplacesBothSides() {
        scoreboard.startGame(LAKERS, CELTICS);

        Game updated = scoreboard.updateScore(LAKERS, CELTICS, 102, 98);

        assertEquals(new Score(102, 98), updated.score());
        assertEquals(List.of("Lakers 102 - Celtics 98"), lines());
    }

    @Test
    void anInvalidScoreLeavesTheGameUnchanged() {
        scoreboard.startGame(LAKERS, CELTICS);
        scoreboard.updateScore(LAKERS, CELTICS, 10, 8);

        assertThrows(IllegalArgumentException.class, () -> scoreboard.updateScore(LAKERS, CELTICS, -1, 8));

        assertEquals(List.of("Lakers 10 - Celtics 8"), lines());
    }

    @Test
    void updatingAGameThatIsNotLiveIsRejected() {
        assertThrows(EntryNotFoundException.class, () -> scoreboard.updateScore(LAKERS, CELTICS, 1, 0));
    }

    private List<String> lines() {
        return scoreboard.summary().stream().map(Game::display).toList();
    }
}
