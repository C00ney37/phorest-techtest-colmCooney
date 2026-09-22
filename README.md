# Fruit Machine

[![CI](https://github.com/C00ney37/phorest-techtest-colmCooney/actions/workflows/ci.yml/badge.svg)](https://github.com/C00ney37/phorest-techtest-colmCooney/actions/workflows/ci.yml)

A virtual fruit machine as a Spring Boot service. Set up a machine (slot count, colours, `k`, play cost, starting float), then play it over REST. Game rules are plain Java with no Spring dependency; the web layer sits on top.

## Running it

Requires JDK 21+. No local Maven install needed — use the wrapper.

```bash
./mvnw test              # run the tests (134 tests)
./mvnw spring-boot:run   # start the app on :8080
```

Or with Docker:

```bash
docker build -t fruitmachine .
docker run -p 8080:8080 fruitmachine
```

Once running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- Health/metrics: `http://localhost:8080/actuator/health`, `.../actuator/metrics/fruitmachine.plays`

### API

| | |
|---|---|
| `POST /machines` | Create a machine. Only `playCost` and `startingFloat` are required — slot count, colours and `k` default to the original game (4 slots, black/white/green/yellow, `k=2`). |
| `GET /machines/{id}` | Read a machine's config and current float/free plays. |
| `POST /machines/{id}/plays` | Play it once. |

Full request/response shapes and validation rules are in Swagger. Quick example:

```bash
curl -X POST localhost:8080/machines -H 'Content-Type: application/json' \
  -d '{"playCost": 100, "startingFloat": 10000}'

curl -X POST localhost:8080/machines/{id}/plays
```

Money is always in minor units (cents).

## Design decisions

The brief leaves several rules ambiguous on purpose. What I decided, and why:

| Decision | Reasoning |
|---|---|
| Money is `long` minor units everywhere | No floating-point rounding errors |
| Stake is added to the float *before* the spin is judged | So "jackpot pays the entire float" includes the money just paid in |
| Full house pays half the float, rounded down | The machine never overpays; keeps the odd unit |
| One prize per spin — jackpot > full house > small prize | Needed once `k` can equal the slot count (a full row would otherwise qualify for two prizes); it's just rule order |
| Shortfall pays what's left, remainder credited as free plays rounded up | A shortfall of 350 at cost 100 credits 4 free plays, not 3.5. Jackpot can never have a shortfall (its prize *is* the float) |
| Free plays are per machine, not per player | The brief has no player/account concept |
| `k` means "a run of *at least* `k`", range `2..slotCount` | Matches "two or more adjacent" generalised; `k=1` would make every spin a winner |
| Small-prize check is a single linear pass (run-length counter) | Stays O(slots) regardless of `k`, rather than re-checking a window at every position |
| Limits: 100,000 slots, 10,000 colours, money capped | Keeps a single response bounded and avoids overflow |
| Two validation layers: Bean Validation for single fields, `MachineConfig` itself for cross-field rules (duplicate colours, `k` > slots) | Field errors are precise; a specific `InvalidMachineConfigException` maps to 400 without catching every `IllegalArgumentException` and hiding real bugs as client errors |
| Unknown JSON fields are rejected | Optional fields default silently otherwise — a typo like `slotCnt` would otherwise create the wrong machine with a 201, not an error |
| In-memory store, one lock per machine in the service | Repository stays a dumb save/find so it could become a real database later; locking protects a single instance, not a cluster |

### Layout

`domain` (plain data, no framework code) → `service` / `service.rule` (game rules, interface + `Impl`, prizes as a strategy list) → `repository` (storage port) → `controller` / `dto` (HTTP) → `config` (all Spring wiring, kept out of the other packages).

### Testing

Unit tests per rule and validation case; a concurrency test (16 threads × 500 plays on one machine, checking money/free-plays balance exactly — verified it can actually fail by removing the lock first); MockMvc tests for the HTTP contract and errors; a full `@SpringBootTest` playing over real HTTP; OpenAPI and metrics tests.

## Left out / next steps

- **No reconfigure or delete endpoint.** Create a new machine instead.
- **State is in-memory only** — lost on restart, and the lock only protects a single running instance. Swapping in a real database is contained to the `repository` package; the lock would need to become optimistic locking (`@Version`).
- **No player/account concept**, so free plays are machine-wide.
- **No auth or rate limiting** — out of scope for the brief.
- **Add acceptance tests (Cucumber).**

CI ([.github/workflows/ci.yml](.github/workflows/ci.yml)) runs the test suite on every push, then builds the Docker image and smoke-tests it — the Dockerfile is verified there rather than on this machine.

## AI usage

Built with Claude Code. Full log of what was used, where it was steered, and what was reworked: [docs/ai-usage-log.md](docs/ai-usage-log.md).
