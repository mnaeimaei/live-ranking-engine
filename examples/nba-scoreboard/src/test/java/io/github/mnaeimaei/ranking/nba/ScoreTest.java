package io.github.mnaeimaei.ranking.nba;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreTest {

    @Test
    void aGameStartsAtZeroZero() {
        assertEquals(new Score(0, 0), Score.ZERO);
    }

    @Test
    void totalIsTheSumOfBothSides() {
        assertEquals(213, new Score(110, 103).total());
    }

    @Test
    void negativePointsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Score(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Score(0, -1));
    }
}
