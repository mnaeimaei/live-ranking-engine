package io.github.mnaeimaei.ranking.nba;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixtureTest {

    private static final Team LAKERS = new Team("Lakers");
    private static final Team CELTICS = new Team("Celtics");

    @Test
    void aTeamCannotPlayItself() {
        assertThrows(IllegalArgumentException.class, () -> new Fixture(LAKERS, new Team(" Lakers ")));
    }

    @Test
    void bothTeamsAreRequired() {
        assertThrows(NullPointerException.class, () -> new Fixture(null, CELTICS));
        assertThrows(NullPointerException.class, () -> new Fixture(LAKERS, null));
    }

    @Test
    void knowsWhichTeamsItInvolves() {
        Fixture fixture = new Fixture(LAKERS, CELTICS);

        assertTrue(fixture.involves(LAKERS));
        assertTrue(fixture.involves(CELTICS));
        assertFalse(fixture.involves(new Team("Bulls")));
    }
}
