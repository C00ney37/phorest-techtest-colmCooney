# AI usage log

How I used AI (Claude Code) on this project: what worked, where I steered it, what I changed. Appended per stage.

## Stage 1: Plan and scaffold

- Gave Claude the brief and asked for a staged plan with one commit per stage. It checked the environment and asked me about build tool, extras and state storage before proposing anything. I reviewed the plan before any code was written.
- Scaffolded from start.spring.io, then tidied the generated project (removed empty pom placeholders, renamed the application class).

## Stage 2: Core spin and jackpot (Part 1)

- Claude drafted the domain model with a minimal `PlayOutcome` so Part 2 extends it rather than replacing it. Tests use a scripted `Spinner` and a seeded `Random`, so they are deterministic.
- I rejected the first draft's use of `var` and one-letter lambda parameters. Reworked to explicit types and descriptive names.
- I redirected the structure: services are an interface plus `Impl` (`Spinner` / `SpinnerImpl`) in a `service` package, kept separate from the `domain` data objects. Claude's draft had descriptively named implementations and a game class inside `domain`.
