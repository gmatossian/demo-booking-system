# Acceptance and proportionate verification

Checklist for the accepted baseline (D1–D10, [decisions.md](decisions.md)). Ticks
record the implementer's verification on 2026-09-25, **not** Gabriel's acceptance,
which is still pending. This is not a production certification.

## Observable outcomes

- [x] Browse rooms, check availability, create/view/cancel a reservation end to end.
  *(browser walkthrough)*
- [x] A valid booking appears consistently in availability and reservation views.
  *(walkthrough; `createdReservationAppearsInScheduleListAndDetail`)*
- [x] Invalid room/booker, missing fields, invalid time ranges, and agreed time
  restrictions are handled consistently by frontend and backend.
  *(`BookingTimePolicyTest`, `invalidRequestsReportFieldErrorsAndChangeNothing`,
  `booking-rules.spec.ts`; UI showed the same "Start time has already passed."
  message from the client and the server)*
- [x] Overlapping active reservations for one room are rejected; different rooms,
  adjacent time ranges, and cancelled bookings follow the accepted rules.
  *(`intervalsAreHalfOpenAndConflictsArePerRoom`, `cancellationReleasesTheSlotAndKeepsHistory`)*
- [x] Two competing overlapping requests cannot both succeed for the same room.
  *(`ConcurrentBookingTest`; see record below)*
- [x] Cancellation releases availability and retains the agreed visible history;
  repeated cancellation follows the documented rule, including after the start time.
  *(`repeatedCancellationReturnsTheUnchangedRecordEvenAfterTheStartTime`,
  `activeReservationCannotBeCancelledOnceStarted`)*
- [x] Timezone and date/time behaviour are explicit and match the accepted policy,
  including daylight-saving cases relevant to that policy.
  *(autumn 2026 change: offsets +01:00/Z, 25-hour day, repeated/skipped local times
  rejected; frontend formatting independent of runtime timezone)*
- [x] Loading, empty, missing-record, conflict, and service-error states are clear.
  *(walkthrough: unknown room/reservation/route, conflict panel, backend stopped)*
- [x] Failed submission preserves input; uncertain outcomes do not silently retry.
  *(walkthrough: backend stopped mid-session → "could not confirm whether this
  booking was saved", inputs kept; after restart no booking had been created)*
- [x] Primary controls have labels and keyboard access; user-entered text is safe.
  *(walkthrough: cancel dialog opened, dismissed with Escape, confirmed with
  Tab/Enter; title `Overlap <b>test</b> & "quotes"` rendered as literal text)*
- [x] Fictional data and demo-only use are clear; no fake identity is called auth.
- [x] Seed/reset restores only the agreed dataset. Startup preserves existing work.
  *(`DemoDataTest`; UI reset and `curl` reset; restart kept data without re-seeding)*
- [x] Successful changes survive restart. *(backend restarted on the same data file)*

## Maintainability and reproducibility

- [x] A fresh checkout builds and runs using the recorded prerequisites/commands.
  *(copy of git-visible files only: `npm ci`, frontend tests, `ng build`, `./mvnw test`)*
- [x] Focused files separate UI, HTTP handling, booking rules, and persistence.
- [x] Documentation states actual capabilities, contracts, data location, and limits.
  *([README](../README.md), [api.md](api.md))*
- [x] Runtime data, credentials, and generated output are excluded appropriately.
  *(`backend/data/`, `backend/target/`, `frontend/node_modules/`, `.angular/`, `dist/` ignored)*
- [x] No future exercise has been implemented as unrequested baseline scope.

## Verification record (2026-09-25)

Environment: macOS, OpenJDK 25.0.4.1, Node 24.20.0 / npm 11.19.0, Maven wrapper 3.9.16.

| Check | Command / method | Result |
| --- | --- | --- |
| Backend tests | `cd backend && ./mvnw test` | 24 run, 0 failures |
| Frontend tests | `cd frontend && npm test -- --watch=false` | 13 passed (3 files); 17 passed (4 files) after the follow-up below |
| Frontend build | `cd frontend && npx ng build` | Succeeded, no warnings |
| Race test is meaningful | Room lock disabled temporarily, `./mvnw test -Dtest=ConcurrentBookingTest`, lock restored | Failed as expected: 7 of 10 simultaneous requests succeeded in round 1 |
| Race test stability | `ConcurrentBookingTest` run 5 times (25 rounds × 10 requests) | All passed: exactly one `201`, nine `409 reservation_conflict` naming the winner, each round |
| Fresh checkout | git-visible files copied to a scratch directory; `npm ci`, tests, build, `./mvnw test` | All passed |
| API probes | `curl` against a running backend | Conflict 409 with details, adjacent 201, field-level 400s, 404s, idempotent cancel, 409 on started cancel, reset summary |
| UI walkthrough | Backend `./mvnw spring-boot:run` + `npm start`, built-in browser at desktop and 375 px widths | Flows above; found and fixed three UI bugs (see below) |
| Restart persistence | Stopped and restarted backend | 12 reservations kept, no re-seed; failed "outage" booking absent |

Bugs found in the walkthrough and fixed before handoff: the booking-time validator ran
before the venue input existed (blank form); a stale "room is free" preview stayed
visible after booking; the reservation filter did not show the selected view on load.
The frontend also now reports a dev-proxy 502/503/504 as "service could not be reached".

### Follow-up fixes (2026-09-25, after first review)

| Check | Command / method | Result |
| --- | --- | --- |
| Ports moved to 8082 / 4302 | Backend `./mvnw spring-boot:run`, frontend `npm start` with defaults | Each port had one listener, running from this repo's `backend/` / `frontend/`; `/api/venue` via 4302 matched 8082 directly; pre-existing Node listeners (4200, 4312, 5178) still running; none stopped |
| Obsolete availability checks | New `booking-form.spec.ts` with delayed (`Subject`) responses: late success after an edit, late error after an edit, older check answering after a newer one, check pending when booking starts | All 4 failed before the fix (stale preview/error shown, old request still subscribed) and pass after it; the pending check is now unsubscribed, which aborts its HTTP request |
| Regression | Full frontend tests, `ng build`, backend `./mvnw test`, browser preview against real backend | 17/17, build clean, 24/24, preview shows and clears on edit |

**Not done:** no automated browser/E2E suite (manual walkthrough only, as agreed in
D10); no cross-browser matrix (Chromium-based built-in browser only); no screen-reader
test; no spring-forward DST test through the API (the policy test covers it, but
28 March 2027 is outside the horizon from the test clock). No load testing.

## Final review

For Gabriel: verified run/reset instructions are in the [README](../README.md);
decisions made during implementation are logged in [decisions.md](decisions.md).
Acceptance, and any Git commit/push, remain your decision.
