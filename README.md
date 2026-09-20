# Movie Ticket Booking System

A Spring Boot backend for a movie ticket booking platform: cities → theaters → screens → shows,
seat-level booking with time-bound holds, tiered pricing + discount codes, simulated payment,
booking confirmation, and policy-driven refunds on cancellation. See **[LLD.md](LLD.md)** for the
full low-level design (entity model, concurrency proof, API list, sequence of operations).

## Tech stack

- **Java 17 / Spring Boot 3.3** — required stack for the assignment.
- **Spring Data JPA + Hibernate** — persistence, plus `@Lock(PESSIMISTIC_WRITE)` repository
  methods for the concurrency-critical seat/discount rows.
- **H2 (file-mode)** — zero-setup persistence; no Docker/DB install needed to run this. Flyway
  manages the schema so switching to Postgres later is a driver/URL change, not a rewrite.
- **Flyway** — versioned schema (`src/main/resources/db/migration`).
- **Spring Security (HTTP Basic)** — the assignment explicitly scopes out OAuth/SSO/MFA and asks
  for "basic RBAC," so Basic Auth + two roles (`ADMIN`, `CUSTOMER`) backed by a DB user table with
  BCrypt password hashes is the deliberately minimal, correct-for-scope choice.
- **Lombok** — cuts entity/DTO boilerplate.
- **JUnit 5 + Mockito + AssertJ + Awaitility** — unit tests for pure logic, `@SpringBootTest`
  integration tests (real H2, real transactions, real HTTP via `TestRestTemplate`) for the
  concurrency and end-to-end flows.

## Key assumptions

See LLD.md §2 for the full list with rationale. The short version:

1. Single service, single DB — no microservices/broker (explicitly out of scope).
2. Auth = username/password + Basic Auth, two roles, no OAuth/SSO.
3. Seat layout lives on `Screen`; creating a `Show` snapshots it into per-show `ShowSeat` rows —
   this is what's actually locked/held/booked.
4. Hold TTL defaults to 5 minutes (`booking.hold-ttl-seconds`), swept every 30s
   (`booking.hold-sweep-interval-ms`), and also lazily expired the instant anything touches a
   stale hold.
5. Pricing = tier base price × (1 + weekend surcharge% if the show falls on Sat/Sun).
6. Discount codes have a global usage cap enforced atomically (row-locked) — never over-redeemed
   under concurrent use.
7. Refund % resolved from admin-configured `RefundPolicy` rows by hours-until-showtime.
8. Payment is **simulated** in-process (`PaymentGateway` interface, `SimulatedPaymentGateway`
   impl) — no real gateway integration, which is out of scope.
9. If a supplied discount code turns out to be invalid/exhausted at confirm time, the **whole
   confirm is rejected** (seats stay `HELD`, hold stays `ACTIVE`) rather than silently booking
   without the discount — the customer can retry without the code before the hold expires.

## Running it

```bash
mvn spring-boot:run
```

Starts on `http://localhost:8080`. On first boot, `DataSeeder` creates a full working demo
dataset automatically:

- **admin** / `admin123` (ROLE_ADMIN)
- **customer1** / `customer123` (ROLE_CUSTOMER)
- City "Bengaluru" → Theater "PVR Forum" → Screen "Screen 1" with 50 seats (rows A–E × 1–10; A/B
  are PREMIUM)
- Movie "Interstellar Returns", pricing tier "Standard" (₹200 regular / ₹350 premium / +20%
  weekend), a `Show` tomorrow at 19:00
- Discount code `WELCOME10` (10% off, capped at 100 uses)
- Refund policies: ≥24h → 100%, ≥2h → 50%, else → 0%

The H2 file database persists under `./data/` between restarts; delete that directory for a clean
slate. Console at `/h2-console` (JDBC URL in `application.yml`).

## Running tests

```bash
mvn test
```

18 tests: unit tests for pricing/discount/refund logic, plus integration tests covering the full
hold → confirm → cancel flow, RBAC, hold expiry sweeping, and — the two tests that matter most for
this assignment's correctness requirement —
`ConcurrentSeatBookingIntegrationTest` (12 customers race for 1 seat; exactly 1 wins) and
`ConcurrentDiscountCodeIntegrationTest` (8 concurrent redeemers against a 3-use code; usage count
never exceeds 3).

## Demo walkthrough (curl)

```bash
# Browse shows
curl http://localhost:8080/api/shows

# Seat map for show 1 (status per seat)
curl http://localhost:8080/api/shows/1/seats

# Hold 2 seats as customer1
curl -u customer1:customer123 -X POST http://localhost:8080/api/shows/1/hold \
  -H "Content-Type: application/json" -d '{"seatIds":[1,2]}'
# -> {"holdId":1, "expiresAt":..., "seatIds":[1,2]}

# Confirm with a discount code
curl -u customer1:customer123 -X POST http://localhost:8080/api/holds/1/confirm \
  -H "Content-Type: application/json" -d '{"discountCode":"WELCOME10","paymentMethod":"CARD"}'

# View my bookings
curl -u customer1:customer123 http://localhost:8080/api/bookings

# Cancel (triggers refund per policy)
curl -u customer1:customer123 -X POST http://localhost:8080/api/bookings/1/cancel

# Admin-only: create a city
curl -u admin:admin123 -X POST http://localhost:8080/api/admin/cities \
  -H "Content-Type: application/json" -d '{"name":"Mumbai"}'
```

## API surface

Full list in LLD.md §5. Summary:

| Area | Method/Path | Role |
|---|---|---|
| Auth | `POST /api/auth/register` | public (creates CUSTOMER) |
| Browse | `GET /api/shows`, `GET /api/shows/{id}`, `GET /api/shows/{id}/seats` | public |
| Booking | `POST /api/shows/{id}/hold`, `DELETE /api/holds/{id}`, `POST /api/holds/{id}/confirm` | CUSTOMER |
| Booking | `GET /api/bookings`, `GET /api/bookings/{id}`, `POST /api/bookings/{id}/cancel` | CUSTOMER (own only) |
| Admin | `/api/admin/cities`, `/theaters`, `/screens`, `/screens/{id}/seats`, `/movies`, `/pricing-tiers`, `/shows`, `/discount-codes`, `/refund-policies` | ADMIN |

## AI-assisted development

This project was built with Claude Code. See **[CLAUDE.md](CLAUDE.md)** for the workflow, prompts
used, and what was reviewed/adjusted by hand.
