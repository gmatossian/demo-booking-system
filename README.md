# demo-booking-system

**Demo and learning purposes only. Not intended for production use.**

A familiar meeting-room booking system built as a reusable baseline for later
practice and ExecDesk dogfooding. Exercises need not become permanent baseline changes.
All rooms, people, and reservations are fictional. Choosing a person in the app
is not authentication. This is a demo intended for public sharing, not a hosted or production-ready
service.

## What works

- **Browse rooms:** five fictional rooms with location, descriptive capacity, and equipment.
- **Room schedule:** active bookings for a room on a chosen day, in venue time (`Europe/London`).
- **Book a room:** title, fictional booker, date, and start/end in 15-minute steps
  between 08:00 and 20:00, up to 90 days ahead.
  - The backend checks availability on every create; checking first is optional.
  - Overlapping bookings for the same room are rejected with the conflicting times,
    including under simultaneous requests. Back-to-back bookings and other rooms are fine.
- **Check availability:** an optional preview that reserves nothing.
- **Reservations list:** filter by Upcoming, Past, Cancelled, or All. The detail page
  shows status, times, and history.
- **Cancel:** confirm in a dialog. Cancelled records stay visible and free the room.
  - A reservation that has started can't be cancelled.
  - Repeating a cancel returns the unchanged record.
- **Visible states:** loading, empty, validation, conflict, not-found, and service-down
  states are all shown.
  - A failed submission keeps what you entered.
  - An unconfirmed booking outcome is reported honestly and never retried automatically.
- **Reset demo data:** a footer button with confirmation, or an API call (below).
  Ordinary startup keeps existing data.
- **Persistence:** data survives restarts, in an embedded H2 database file.

Rules, fields, and endpoints: [docs/api.md](docs/api.md). Decisions: [docs/decisions.md](docs/decisions.md).

## Prerequisites

Verified on macOS with these versions:

| Tool | Required | Verified with |
| --- | --- | --- |
| JDK | 25 (Spring Boot 4.1.1 build targets Java 25) | OpenJDK 25.0.4.1 |
| Node.js | `^22.22.3 \|\| ^24.15.0 \|\| >=26` (Angular 22.2.0) | 24.20.0, npm 11.19.0 |
| Maven | Not needed; `backend/mvnw` downloads Maven 3.9.16 | — |

No global Angular CLI is needed; the project's pinned CLI runs via npm scripts.
If `./mvnw` reports an unsupported class version, point `JAVA_HOME` at a JDK 25.

## Run

Use two terminals. The backend listens on **8082** and the frontend on **4302**, so
they don't clash with sibling demos on 8080/4200.

Backend (the first run downloads dependencies):

```bash
cd backend && ./mvnw spring-boot:run
```

Frontend (install once with `npm ci`, then start):

```bash
cd frontend && npm ci && npm start
```

Open <http://localhost:4302>. Stop either process with `Ctrl+C`.

If 4302 is taken, pass another port with `npm start -- --port 4290`. To move the
backend, change `server.port` in `backend/src/main/resources/application.properties`
and the target in `frontend/proxy.conf.json` together.

## Data and reset

- Data lives in `backend/data/booking.mv.db`, relative to where the backend starts.
  Set `BOOKING_DATA_DIR` to use another directory. The folder is git-ignored.
- On startup, an empty database is seeded with fixtures. An existing database is left alone.
- **Reset demo data** replaces all rooms, people, and reservations with fresh
  fixtures dated relative to today in venue time. Anything you booked is deleted.
  Use the footer button in the app, or:

```bash
curl -X POST http://localhost:8082/api/demo/reset
```

- To start from a completely empty database, stop the backend and delete
  `backend/data/`. The next start recreates the schema and seeds it.

## Tests

Backend: 24 tests. They cover time rules, booking rules through the real HTTP API,
the competing-request check, and reset/seeding. They use a disposable H2 file under
`backend/target/` and a controllable clock.

```bash
cd backend && ./mvnw test
```

Frontend: 17 unit tests covering venue-time formatting, API error mapping, client-side
time rules, and discarding obsolete availability checks when their answers arrive late.

```bash
cd frontend && npm test -- --watch=false
```

The competing-request test sends 10 simultaneous overlapping requests for one room,
over five rounds. It asserts exactly one `201` and a `409 reservation_conflict` for
every other request. With the room lock temporarily removed, it fails: 7 of 10
requests succeeded in the first round. See [docs/acceptance.md](docs/acceptance.md)
for the full verification record.

## Layout

```text
backend/    Spring Boot API: venue/, room/, person/, reservation/, demo/, web/ packages
frontend/   Angular app: core/ (API, time), rooms/, reservations/, shared/, layout/
docs/       Brief, decisions, API contract, acceptance record, optional exercises
```

## Known limits

- **Single instance only.** The booking guarantee relies on a row lock inside one
  local H2 database. There is no database-level overlap constraint, and running
  several backend instances against separate databases would not share it.
  See the "More than one app instance" exercise.
- **No real users.** There's no authentication or authorization; anyone can book or
  cancel anything. The booker selector is a label.
- **No editing or rescheduling.** There are also no recurring bookings, waitlists, or
  notifications. Those are future exercises; see [docs/exercises.md](docs/exercises.md).
- **No pagination.** Lists return everything, which is fine for demo-sized data.
- **Stale "today" in the browser.** The frontend loads the venue's "today" once per
  page load. After midnight, reload before booking; the backend always enforces the real date.
- **Dev servers only.** There's no production build or deployment setup.
  `ng build` output isn't served by the backend.
- **Flyway warning.** Flyway 12.4 logs that H2 2.4.240 is newer than its latest
  verified H2 version; migrations run correctly.

## Delegation pack

| Document | Purpose |
| --- | --- |
| [CLAUDE.md](CLAUDE.md) | Working instructions and authority boundaries. |
| [Product brief](docs/product-brief.md) | Purpose, audience, and baseline discipline. |
| [Decisions](docs/decisions.md) | Accepted decisions and choices made during implementation. |
| [Baseline specification](docs/baseline-spec.md) | Capabilities and booking rules. |
| [API and data](docs/api.md) | Fields, lifecycle, time representation, endpoints, errors. |
| [Implementation brief](docs/implementation-brief.md) | How the baseline was to be built and handed back. |
| [Acceptance](docs/acceptance.md) | Outcomes and the verification record. |
| [Exercises](docs/exercises.md) | Optional future work, excluded from the baseline. |
| [Handoff prompt](docs/handoff-prompt.md) | The original delegation prompt. |

Scope approval does not imply permission to commit, push, merge, deploy, or publish.
