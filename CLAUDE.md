# AI Workflow Used During Development

This file documents how Claude Code was used to build this project, per the assignment's
requirement to disclose the AI workflow. Nothing here is a project instruction file (there's no
separate `AGENTS.md`) — this *is* the record of the session.

## Tooling

- **Assistant:** Claude Code CLI (Claude Sonnet 5), interactive session.
- **Skills/plugins:** none. This is a generic Java/Spring Boot task with no project-specific
  skill available, so the assistant worked directly with its built-in file (Read/Write/Edit) and
  shell (Bash) tools — no custom skill or subagent orchestration was invoked.
- **Human in the loop:** the developer picked which of the four candidate systems to build (with
  the assistant's comparative recommendation), approved the overall approach, and reviewed the
  code, tests, and live curl walkthrough before sign-off.

## Sequence of work

1. **Compare the four options.** The assistant read all four assignment PDFs and compared them on
   build complexity and how demoable each is on camera, and recommended Movie Ticket Booking
   System (narrowest role model, single clear concurrency hotspot, easy to show working live).
2. **LLD first.** Before any code, the assistant wrote `LLD.md`: entity model (ER diagram),
   the concurrency design and correctness argument for seat holds (this was the part reasoned
   through most carefully — see LLD.md §4), API surface, pricing/discount/refund logic, and the
   testing strategy. This became the spec the rest of the session implemented against.
3. **Schema → entities → repositories.** Flyway migration written by hand from the LLD's ER
   diagram, then JPA entities and Spring Data repositories, including the
   `@Lock(PESSIMISTIC_WRITE)` query methods that are the crux of the concurrency design.
4. **Service layer, then controllers.** Business logic (pricing, discount locking, refund
   resolution, the hold/confirm/cancel lifecycle, async notification dispatch) written before the
   REST layer, so the concurrency-critical transaction boundaries were decided deliberately rather
   than inferred from a controller shape.
5. **Compile early, compile often.** `mvn compile` was run right after the entity/repository layer
   (before writing services/controllers) to catch mapping mistakes early, and again after the full
   scaffold.
6. **Tests.** Unit tests (pricing, discount cap, refund-policy boundary logic) written against
   mocked repositories. Integration tests written last, against a fresh `TestDataFactory` (so
   tests don't contend on shared seeded rows) — including the two tests that matter most for this
   assignment's stated requirement: `ConcurrentSeatBookingIntegrationTest` (N threads race for one
   seat) and `ConcurrentDiscountCodeIntegrationTest` (concurrent redemption never exceeds the
   usage cap).
7. **Debugging loop.** Running the full suite surfaced three real bugs, fixed in order:
   - `value` as a column name is an H2 reserved word → renamed to `discount_value` in both the
     Flyway migration and the `@Column` mapping.
   - `NotificationService` hit `LazyInitializationException` because the `@Async` listener called
     a sibling `@Transactional` method via plain `this.dispatch(...)` — a Spring AOP self-invocation
     bug (the proxy is bypassed on internal calls). Fixed by moving `@Transactional` onto the
     listener methods themselves.
   - Spring then rejected `@Transactional` on a `@TransactionalEventListener(phase=AFTER_COMMIT)`
     method under default propagation (the original transaction has already committed by the time
     the async listener runs) → fixed with `propagation = REQUIRES_NEW`.
8. **Live smoke test.** After tests passed, the app was actually started (`mvn spring-boot:run`)
   and driven end-to-end with `curl`: browse → seat map → hold → confirm with discount → list
   bookings → cancel → verify refund + seat freed → verify RBAC (customer blocked from admin
   routes, unauthenticated blocked entirely). This step is why the assignment's "test the golden
   path in a real running instance, not just the test suite" bar is met.

