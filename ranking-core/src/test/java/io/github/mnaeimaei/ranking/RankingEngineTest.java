package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingEngineTest {

    @Test
    void newEngineHasEmptyRanking() {
        RankingEngine<String, Integer> engine = new RankingEngine<>();

        assertTrue(engine.ranking().isEmpty());
    }
}
