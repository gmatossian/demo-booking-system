# Decision register

Handoff recorded 2026-09-25 (documentation only). Baseline choices below were
accepted by Gabriel on 2026-09-25 as recommendations D1–D10 with four adjustments.

## Accepted direction

- A separate, understandable demo system for later learning and ExecDesk dogfooding.
- Build basic features first; exercise solutions need not change the baseline.
- Angular frontend with one Java/Spring Boot backend service.
- Readable, maintainable code and proportionate checks.
- Consequential decisions and final acceptance remain with Gabriel unless delegated.

## Accepted baseline decisions (2026-09-25)

| ID | Topic | Accepted decision |
| --- | --- | --- |
| D1 | Scope and backend | Meeting-room baseline B1–B8 with the exclusions in [baseline-spec.md](baseline-spec.md); no room or person administration. One Spring Boot service is the authority for conflict checking and shared durable state. |
| D2 | Data model | ~5 fixed fictional rooms (name, location, descriptive capacity, descriptive equipment tags); each reservation claims the whole room. ~5 fictional people. Reservations have a required plain-text title (1–80 characters). Conflicts are per room only; one person may hold overlapping reservations in different rooms. |
| D3 | Demo identity | The booker is chosen per booking in the form; there is no global "acting as" state. Everyone may view and cancel every reservation. A demo banner states that data is fictional and there is no authentication. |
| D4 | Time | Venue timezone `Europe/London`, displayed with times; browser/server timezones are never used for booking semantics. Bookable every day 08:00–20:00 venue time, 15-minute granularity, start and end on the same local day, minimum 15 minutes, no maximum beyond opening hours. No start in the past; start date at most 90 days after the venue's current date. Nonexistent/ambiguous local times are rejected (with these hours they are already outside opening hours). The API accepts venue-local date-times such as `2026-10-25T09:00`; responses carry offsets; storage is UTC. |
| D5 | Overlap and lifecycle | Intervals are `[start, end)`; adjacent reservations are allowed. Only `ACTIVE` reservations block. Lifecycle is `ACTIVE → CANCELLED`; no edits. "Past"/"in progress" are derived display phases, not stored states. |
| D6 | Cancellation | Explicit confirmation in a native `<dialog>`. Cancelling an already-cancelled reservation returns `200` with the unchanged record — **checked before the start-time restriction**, so a repeat cancel succeeds even after the start time (adjustment 3). Otherwise a reservation that has started or ended cannot be cancelled (`409`). Cancelled records remain visible with a cancellation timestamp. |
| D7 | Persistence and enforcement | Embedded H2 in file mode under `backend/data/` (git-ignored), Flyway migrations, explicit SQL via Spring `JdbcClient`. Create runs in one transaction: lock the room row (`SELECT … FOR UPDATE`), check active overlaps, insert. Single-instance local deployment only. |
| D8 | Reset and fixtures | Ordinary startup seeds only an empty database and otherwise preserves existing work. Reset is available from a **"Reset demo data" UI button with confirmation plus a documented API command** (adjustment 1); it replaces only the demo application data (rooms, people, reservations) in one transaction. Fixture dates are relative to the venue's current date at seed/reset time. |
| D9 | UX | Plain CSS, native controls, no component library. Screens: room list; room page with a day schedule and booking form; reservation list with Upcoming/Past/Cancelled/All filter; reservation detail with cancel. Checking availability is optional — **booking does not require a prior check; creation always enforces availability and shows conflicts clearly** (adjustment 2). Failed submissions preserve input; uncertain outcomes are reported honestly, never auto-retried. The demo banner stays visible but unobtrusive. |
| D10 | Technical outline | `backend/` Spring Boot 4.1 / Java 25 / Maven wrapper, packaged by feature with separate controller/service/repository; RFC 9457 problem responses. `frontend/` Angular 22 standalone components and signals, dev proxy to the API. Framework versions pinned exactly; global installs left unchanged. The competing-request test must show **exactly one success and explicit `409` conflict responses for every other request** (adjustment 4), not merely failures. Main UI path verified manually against the real backend. |

## Decisions made during implementation

Routine choices made while building, within the accepted outline. Gabriel may
revisit any of them at review.

| Date | Decision | Reason |
| --- | --- | --- |
| 2026-09-25 | Reset is exposed as `POST /api/demo/reset` (UI button and `curl`); the command-line reset flag from the original D8 proposal was not added. | Adjustment 1 named the UI button and an API command; a third mechanism adds code without new value. |
| 2026-09-25 | Default ports are 8082 (backend) and 4302 (frontend dev server), set by Gabriel after review; they replace the interim 8081/4201. | The sibling demos use 8080/4200, and another local project was later seen on 4201; distinct ports let them run side by side. |
| 2026-09-25 | Room and person IDs are fixed by the fixtures (1–5); reservation IDs are generated, increasing, and never reused after a reset. | Keeps reset transactional in H2 (no identity-restart DDL) and means an old reservation link after reset shows "not found" instead of different data. |
| 2026-09-25 | Reset replaces rooms, people, and reservations — the whole application dataset, all of which is demo data. | Adjustment 1 said "only demo application data"; there is no other data in this app. Schema history is untouched. |
| 2026-09-25 | Problem responses carry a stable `code` (`validation_failed`, `reservation_conflict`, `cancellation_not_allowed`, `not_found`) and, for 409 conflicts, the conflicting reservations. | Lets the UI show conflicts clearly and lets the competing-request test assert explicit conflict responses (adjustment 4). |
| 2026-09-25 | Only one free-text field (title, 1–80 characters, trimmed). Booker names in conflicts/schedules are shown because all people are fictional. | D2; the brief asked for useful conflict information without real personal data. |
| 2026-09-25 | Versions pinned: Spring Boot 4.1.1 (Java 25, Maven 3.9.16 via wrapper; resolves Spring Framework 7.0.9, Jackson 3.1.5, H2 2.4.240, Flyway 12.4.0); Angular 22.2.0, TypeScript 6.0.3, Vitest 5.0.2, all exact in `package.json` with a lockfile. | D10: verified released, mutually compatible versions; global installs unchanged. |
| 2026-09-25 | Frontend reads use Angular `httpResource` (stable in v22); writes use `HttpClient`. No component library, no state library. | Keeps components small and data loading declarative without extra dependencies. |

## Updating decisions

Record material answers, date, and whether Gabriel accepted or explicitly delegated
them. Update the relevant specification/checklist too. Do not convert suggestions
into agreements just because code has been written. Git delivery and publication require separate authority; the initial baseline
delivery authorization is recorded below.

## Baseline delivery authorization — 2026-09-25

Gabriel authorized committing and pushing the reviewed baseline, with a merge only
if the work was on a separate branch. The work was already on main. This authority
is specific to the initial baseline delivery; it does not grant standing permission
to merge exercise solutions, publish/deploy, or change repository visibility.
Hands-on acceptance remains separate from this delivery checkpoint.

## Public sharing — 2026-09-25

Gabriel authorized making this repository public after checking for secrets,
sensitive data, and machine-specific paths, superseding the earlier private-only
publication boundary. Preparation removes local checkout paths and incidental
process identifiers from documentation where present. Git history must be checked
as well as the current files before changing visibility. Public source availability
does not authorize deployment or promotion of exercise solutions into main.
