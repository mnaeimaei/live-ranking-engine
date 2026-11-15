# live-ranking-engine

[![build](https://github.com/mnaeimaei/live-ranking-engine/actions/workflows/build.yml/badge.svg)](https://github.com/mnaeimaei/live-ranking-engine/actions/workflows/build.yml)
![Java 17+](https://img.shields.io/badge/java-17%2B-blue)
![dependencies: none](https://img.shields.io/badge/runtime%20dependencies-none-brightgreen)

A small, thread-safe, in-memory Java library that keeps **live entries in ranked order**, built entirely with
**test-driven development**. The rules are yours: you supply the ordering and the domain rules, and the engine
handles storage, determinism and concurrency.

The reference use case is a **live NBA scoreboard**, but the same engine ranks ML training runs, game players,
auction bids or support tickets without changing a line.

```java
NbaScoreboard board = new NbaScoreboard();
board.startGame(new Team("Suns"), new Team("Mavericks"));
board.startGame(new Team("Lakers"), new Team("Celtics"));
board.updateScore(new Team("Suns"), new Team("Mavericks"), 120, 118);

board.summary();   // [Suns 120 - Mavericks 118, Lakers 0 - Celtics 0]
board.startGame(new Team("Heat"), new Team("Lakers"));   // throws TeamAlreadyPlayingException
```

---

## The problem

Many systems hold a small set of *live* things that change constantly and must be shown in rank order:
games on a scoreboard, runs on an experiment leaderboard, bids in an auction. Each version of the problem has the
same core and different rules:

| Concern | Same everywhere | Domain-specific |
|---|---|---|
| Storage | add, update, remove by key | what the key and value are |
| Ranking | sort on read, deterministic ties | the ordering itself |
| Validation | reject duplicates, unknown keys, nulls | rules such as "a team plays one game at a time" |
| Concurrency | no lost updates, atomic check-then-act | — |

`live-ranking-engine` implements the left column once, and lets each domain plug in the right column.

## Modules

```
live-ranking-engine/
├── ranking-core/              the library (no runtime dependencies)
│   └── io.github.mnaeimaei.ranking
│       ├── RankingEngine      thread-safe engine + fluent Builder
│       ├── RankedEntry        immutable snapshot record (key, value, sequence)
│       ├── Orderings          highestFirst(score), mostRecentFirst()
│       ├── AdmissionRule      extension point for domain rules
│       └── *Exception         RankingException hierarchy
└── examples/nba-scoreboard/   the reference use case
    └── io.github.mnaeimaei.ranking.nba
        ├── NbaScoreboard      facade: startGame, updateScore, finishGame, summary
        ├── Team, Score, Fixture, Game   validated value records
        ├── OneLiveGamePerTeam           AdmissionRule
        └── TeamAlreadyPlayingException
```

## Using the core library

```java
record Run(String model, double validationAccuracy) {}

RankingEngine<String, Run> leaderboard = RankingEngine
        .orderedBy(Orderings.<String, Run>highestFirst(Run::validationAccuracy)
                .thenComparing(Orderings.mostRecentFirst()))
        .withAdmissionRule((runId, run, live) -> {
            if (live.stream().filter(r -> r.model().equals(run.model())).count() >= 2) {
                throw new EntryRejectedException(run.model() + " already has 2 live runs");
            }
        })
        .build();

leaderboard.add("run-1", new Run("resnet50", 0.91));
leaderboard.update("run-1", run -> new Run(run.model(), 0.95));
leaderboard.top(3);           // immutable snapshot, best first
leaderboard.remove("run-1");  // returns the final value
```

This exact example is an executable test:
[`ExperimentLeaderboardExampleTest`](ranking-core/src/test/java/io/github/mnaeimaei/ranking/ExperimentLeaderboardExampleTest.java).

| Operation | Behaviour |
|---|---|
| `add(key, value)` | Rejects a live key (`EntryAlreadyExistsException`) or a vetoed entry (`EntryRejectedException`). |
| `update(key, fn)` | Atomic read-modify-write; keeps the entry's original recency. `EntryNotFoundException` if not live. |
| `remove(key)` | Returns the final value. `EntryNotFoundException` if not live. |
| `ranking()`, `top(n)` | Immutable snapshots, best first. |
| `find(key)`, `size()` | Point lookups. |

## Design decisions

- **Generic engine, domain in plug-ins.** The engine is `RankingEngine<K, V>` and knows nothing about sports.
  Orderings are plain `Comparator`s (Strategy pattern), and domain rules are `AdmissionRule`s. The NBA module
  contains no ranking or locking code at all; it only describes its domain.
- **Fail loudly.** Invalid operations throw specific exceptions instead of being silently ignored. That makes the
  API honest and the tests precise: a test can tell "rejected on purpose" apart from "a bug did nothing".
- **Always deterministic.** The engine appends insertion order as the final tie-breaker, so two entries never
  compare equal and the ranking never depends on hash order.
- **Recency survives updates.** `RankedEntry.sequence` is set once, on `add`. Updating a score never makes a game
  "newer", which is exactly what "most recently started" needs.
- **Immutable values and snapshots.** Domain types are validating `record`s (`Score` rejects negative points,
  `Fixture` rejects a team playing itself). Every read returns an unmodifiable snapshot, so callers can't corrupt
  the engine's state.
- **Concurrency: one `ReentrantReadWriteLock`.** Writes are exclusive, so admission rules and update functions run
  atomically with the change they guard. This is what makes "one live game per team" correct under contention
  (see `NbaScoreboardConcurrencyTest`). Reads share the lock. A single coarse lock is the simplest correct choice
  for a small live set. Lock striping or a lock-free design would only pay off at much larger scale.
- **Sort on read.** `ranking()` sorts on each call (O(n log n)). Live sets are small and score updates are
  frequent, so keeping a sorted index up to date on every write would cost more than it saves here.

## How it was built: TDD

Every behaviour was introduced with a failing test first, then the smallest change that made it pass, then a
refactor while all tests stayed green. **The git history is the evidence**: each step is its own commit, prefixed
`red:`, `green:` or `refactor:`.

```bash
git log --reverse --oneline
```

A few steps worth reading:

| Step | Commits | What it shows |
|---|---|---|
| First test to first behaviour | `19f8e43` → `5b8398d` | Growing an API from the simplest possible test |
| Deterministic ties | `b29dff1` → `433b112` | A red test exposing hash-order nondeterminism |
| Recency survives updates | `18bbcff` → `57de6dd` | Pinning a subtle requirement before it can regress |
| Admission rules | `0056c77` → `8292d26` | Designing an extension point from its usage in a test |
| Concurrency | `1df9c97` → `093a136` | A test that reliably lost most of 80,000 concurrent updates; the fix also removed an O(n²) slowdown the test exposed |
| NBA acceptance scenario | `5703d57` → `f6d901e` | A five-game, end-to-end ordering example, written before the ordering existed |
| One game per team | `84dd729` → `d327241` | A domain rule plugged into the generic engine, catching reversed fixtures too |
| Refactors | `0f5f2e7`, `79e3c00`, `8cf7684`, `57689bb` | Improving design with the tests as a safety net |

Tests are named as specifications (`updateKeepsTheOriginalInsertionSequence`,
`aTeamCannotPlayTwoLiveGamesAtOnce`) and test behaviour through public APIs only.

## Build and test

Requires Java 17+ and Maven 3.6+.

```bash
mvn verify                                  # build both modules and run all 43 tests
mvn -pl ranking-core test                   # library only
mvn -pl examples/nba-scoreboard -am test    # NBA use case (and the core it depends on)
```

CI runs the build on Java 17 and 21 with GitHub Actions.

## Assumptions

- Team names are trimmed, and comparison is case-sensitive (`"Lakers"` and `" Lakers "` are the same team; `"lakers"` is not).
- A game is identified by its fixture (home, away). `Lakers–Celtics` and `Celtics–Lakers` are different fixtures,
  but the one-game-per-team rule prevents both from being live at once.
- Score updates replace the whole score, and corrections downwards are allowed (real scoreboards correct mistakes).
- The engine is in-memory only. Persistence and distribution are out of scope.

## Possible next steps

- Change listeners (`onRankChanged`) so UIs can push updates instead of polling.
- Validation rules on update, not only on admission (for example "scores never decrease").
- A JMH benchmark comparing sort-on-read with an incrementally maintained index.

## License

MIT
