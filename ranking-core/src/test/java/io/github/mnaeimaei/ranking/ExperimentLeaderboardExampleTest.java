package io.github.mnaeimaei.ranking;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The engine is not tied to sports: here it ranks ML training runs by validation accuracy,
 * preferring the most recent run on ties, and caps how many runs one model may have live.
 */
class ExperimentLeaderboardExampleTest {

    record Run(String model, double validationAccuracy) {
    }

    private static AdmissionRule<String, Run> atMostRunsPerModel(int max) {
        return (runId, candidate, live) -> {
            long sameModel = live.stream().filter(run -> run.model().equals(candidate.model())).count();
            if (sameModel >= max) {
                throw new EntryRejectedException(candidate.model() + " already has " + max + " live runs");
            }
        };
    }

    @Test
    void ranksTrainingRunsByAccuracyThenRecency() {
        RankingEngine<String, Run> leaderboard = RankingEngine
                .orderedBy(Orderings.<String, Run>highestFirst(Run::validationAccuracy)
                        .thenComparing(Orderings.mostRecentFirst()))
                .withAdmissionRule(atMostRunsPerModel(2))
                .build();

        leaderboard.add("run-1", new Run("resnet50", 0.91));
        leaderboard.add("run-2", new Run("vit-b16", 0.94));
        leaderboard.add("run-3", new Run("resnet50", 0.94));
        leaderboard.update("run-1", run -> new Run(run.model(), 0.95));

        assertEquals(List.of("run-1", "run-3", "run-2"),
                leaderboard.ranking().stream().map(RankedEntry::key).toList());
        assertThrows(EntryRejectedException.class,
                () -> leaderboard.add("run-4", new Run("resnet50", 0.99)));
    }
}
