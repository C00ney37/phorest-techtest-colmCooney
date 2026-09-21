# AI usage log

How I used AI (Claude Code) on this project: what worked, where I steered it, what I changed. Appended per stage.

## Stage 1: Plan and scaffold

- Gave Claude the brief and asked for a staged plan with one commit per stage. It checked the environment and asked me about build tool, extras and state storage before proposing anything. I reviewed the plan before any code was written.
- Scaffolded from start.spring.io, then tidied the generated project (removed empty pom placeholders, renamed the application class).

## Stage 2: Core spin and jackpot (Part 1)

- Claude drafted the domain model with a minimal `PlayOutcome` so Part 2 extends it rather than replacing it. Tests use a scripted `Spinner` and a seeded `Random`, so they are deterministic.
- I rejected the first draft's use of `var` and one-letter lambda parameters. Reworked to explicit types and descriptive names.
- I redirected the structure: services are an interface plus `Impl` (`Spinner` / `SpinnerImpl`) in a `service` package, kept separate from the `domain` data objects. Claude's draft had descriptively named implementations and a game class inside `domain`.

## Stage 3: Payouts, float and free plays (Part 2)

- Claude proposed a stateless `play(config, state)` that returns the new state in the outcome, with prize rules as an ordered list of strategies, so a new rule is one class plus one list entry. Tests cover each ambiguous rule (stake enters the float first, half rounds down, shortfall rounds up to whole free plays).
- The three rules are distinct implementations of `PrizeRule`, so they keep descriptive names rather than the `Impl` suffix.

## Stage 4: Generalise and scale (Part 3)

- Moved `k` onto `MachineConfig` and replaced the pair check with a single-pass run-length scan, so the cost is O(n) for any `k`. Added upper limits on slots and colours so the API stage has hard bounds to validate against.
- Scale tests run at the limits (100,000 slots, thousands of colours). The large-`k` case is built so that re-checking a window of `k` slots at every position would blow the timeout.

## Stage 5: Machine service, repository and locking

- Claude put the per-machine lock in the service, not the repository, so the store stays a plain save/find that could later become a database. The service checks a machine exists before creating its lock, so made-up ids don't leave locks behind.
- The concurrency test checks that money and free plays balance exactly across 8,000 concurrent plays. I had Claude remove the lock temporarily to prove the test can fail: it failed 3 runs out of 3, then the lock was restored.
