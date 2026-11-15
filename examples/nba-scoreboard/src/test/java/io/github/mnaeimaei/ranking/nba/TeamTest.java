package io.github.mnaeimaei.ranking.nba;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamTest {

    @Test
    void nameIsTrimmedSoTheSameTeamIsAlwaysEqual() {
        assertEquals(new Team("Lakers"), new Team("  Lakers "));
        assertEquals("Lakers", new Team(" Lakers").name());
    }

    @Test
    void blankOrMissingNamesAreRejected() {
        assertThrows(NullPointerException.class, () -> new Team(null));
        assertThrows(IllegalArgumentException.class, () -> new Team(""));
        assertThrows(IllegalArgumentException.class, () -> new Team("   "));
    }
}
