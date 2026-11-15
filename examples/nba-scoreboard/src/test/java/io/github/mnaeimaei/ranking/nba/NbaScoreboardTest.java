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

    @Test
    void summaryRanksByTotalPointsThenByMostRecentlyStarted() {
        start("Lakers", "Celtics", 102, 98);       // 200, started 1st
        start("Warriors", "Nuggets", 115, 112);    // 227
        start("Bucks", "Heat", 99, 101);           // 200, started 3rd
        start("Suns", "Mavericks", 120, 118);      // 238
        start("Knicks", "Bulls", 95, 105);         // 200, started 5th

        assertEquals(List.of(
                "Suns 120 - Mavericks 118",
                "Warriors 115 - Nuggets 112",
                "Knicks 95 - Bulls 105",
                "Bucks 99 - Heat 101",
                "Lakers 102 - Celtics 98"), lines());
    }

    private void start(String home, String away, int homePoints, int awayPoints) {
        scoreboard.startGame(new Team(home), new Team(away));
        scoreboard.updateScore(new Team(home), new Team(away), homePoints, awayPoints);
    }

    private List<String> lines() {
        return scoreboard.summary().stream().map(Game::display).toList();
    }
}
