# Data model and HTTP API

Demo-only contract for the baseline. All data is fictional. The backend runs on
`http://localhost:8082`; in development the Angular server proxies `/api` there.

## Time representation

- The venue timezone is `Europe/London` (`venue.zone` in
  `backend/src/main/resources/application.properties`). Every booking rule uses it.
- **Requests** carry venue-local wall-clock times without an offset:
  `"2026-10-25T09:00"`. The backend interprets them in the venue timezone.
- **Responses** carry ISO-8601 times with the venue's UTC offset in force at that
  moment: `"2026-10-24T09:00:00+01:00"`, `"2026-10-25T09:00:00Z"`.
- **Storage** is UTC (`TIMESTAMP WITH TIME ZONE`).
- The frontend formats times with the venue timezone explicitly. It never uses the
  browser's timezone for booking meaning.

## Records

| Record | Fields | IDs |
| --- | --- | --- |
| Room | `id`, `name`, `location`, `capacity` (descriptive only), `equipment` (descriptive tags) | Fixed 1–5 from the fixtures; stable across resets. |
| Person | `id`, `displayName` | Fixed 1–5 from the fixtures; stable across resets. Choosing one is not authentication. |
| Reservation | see below | Generated, increasing, never reused, even after a reset. |

Reservation response fields: `id`, `roomId`, `roomName`, `personId`, `personName`,
`title`, `start`, `end`, `status`, `phase`, `cancellable`, `createdAt`, `cancelledAt`.

- `status` is stored: `ACTIVE` → `CANCELLED` is the only transition. There is no
  editing or rescheduling.
- `phase` is derived from the current time: `UPCOMING` (not started),
  `IN_PROGRESS`, or `ENDED`. The UI shows active + ended as "Past".
- `cancellable` is true only for `ACTIVE` reservations that have not started.

## Booking rules

A create request is accepted only if all of these hold:

- `roomId` and `personId` refer to existing records.
- `title` is 1–80 characters after trimming surrounding whitespace. It is plain
  text: the UI never renders it as HTML.
- `start` and `end` are on 15-minute boundaries, on the same venue-local day, with
  `start < end`, within opening hours 08:00–20:00.
- The local times exist and are unambiguous in the venue timezone. Daylight-saving
  gaps/overlaps fall at 01:00–02:00, already outside opening hours.
- `start` is not before the current time, and its date is at most 90 days after the
  venue's current date.
- No `ACTIVE` reservation for the **same room** overlaps the half-open interval
  `[start, end)`. Back-to-back reservations are allowed. Different rooms never conflict.

The overlap check and insert run in one transaction after locking the room row
(`SELECT … FOR UPDATE`). Competing requests for one room are therefore serialised,
and exactly one of any set of mutually overlapping requests succeeds. The others
get `409 reservation_conflict`. The availability endpoint takes no lock and is only a preview.

## Errors

Errors use RFC 9457 problem JSON with a stable `code`:

| Status | `code` | When | Extra fields |
| --- | --- | --- | --- |
| 400 | `validation_failed` | Missing/invalid fields or broken booking rules | `errors: [{field, message}]` |
| 400 | *(none)* | Unparseable JSON or query parameter (framework default) | — |
| 404 | `not_found` | Unknown room or reservation | — |
| 409 | `reservation_conflict` | Overlaps an active reservation in the same room | `conflicts: [Reservation]` |
| 409 | `cancellation_not_allowed` | Cancelling an active reservation that has started | — |

A failed create or cancel changes nothing.

## Endpoints

| Method and path | Result |
| --- | --- |
| `GET /api/venue` | Venue name, `zone`, `opensAt`, `closesAt`, `slotMinutes`, `bookingHorizonDays`, `now`, `today`, `lastBookableDate`. |
| `GET /api/rooms` | Rooms ordered by name. |
| `GET /api/rooms/{id}` | One room, or 404. |
| `GET /api/people` | People ordered by name. |
| `GET /api/rooms/{id}/schedule?date=2026-10-21` | `{date, reservations}`: active reservations overlapping that venue-local day, by start. 404 for an unknown room. |
| `GET /api/rooms/{id}/availability?start=…&end=…` | `{available, conflicts}` after the same validation as create (400 if invalid). A preview; reserves nothing. |
| `GET /api/reservations?view=upcoming` | `upcoming` (default: active, not ended, soonest first), `past` (active, ended, latest first), `cancelled` (latest start first), `all` (latest start first). No pagination. |
| `GET /api/reservations/{id}` | One reservation, or 404. |
| `POST /api/reservations` | Body `{roomId, personId, title, start, end}`. `201` with the reservation and a `Location` header, or 400/409. |
| `POST /api/reservations/{id}/cancel` | `200` with the cancelled reservation. If already cancelled: `200` with the **unchanged** record, checked before the start-time rule, so it holds even after the start. Otherwise 409 once started; 404 if unknown. |
| `POST /api/demo/reset` | Replaces all rooms, people, and reservations with fresh fixtures in one transaction. Returns `{anchorDate, rooms, people, reservations}`. |

## Demo fixtures

Defined in `backend/src/main/java/demo/booking/demo/DemoFixtures.java`. Reservation
dates are day offsets from the **venue's current date when seeding or reset runs**
(`anchorDate`), at fixed venue-local times:

- a past active booking (day −2), a past active one (day −1), and a past cancelled one (day −1);
- one booking today at 12:00–13:00, whose phase depends on when you look;
- a back-to-back pair in Aurora (day +1, 09:00–10:00 and 10:00–11:00);
- a cancelled booking (day +2) whose slot is free again;
- further upcoming bookings on days +1, +2, +3 and +7.

Ordinary startup seeds only an empty database. As days pass without a reset,
fixture bookings drift into the past; reset re-anchors them to today.
