# Baseline specification

Status: meeting-room baseline accepted 2026-09-25 (D1–D10 in [decisions.md](decisions.md))
and implemented for review. The exact rules as built are in [api.md](api.md); where
this page says "proposed", the decision register records what was agreed. Human
acceptance of the implementation is still pending.

## Capabilities

| ID | Capability | Draft observable outcome |
| --- | --- | --- |
| B1 | Browse rooms | View a small seeded set of fictional rooms, with name, location, and descriptive capacity. |
| B2 | Check availability | Choose a room and time range; see whether it can be reserved and useful conflict information without real personal data. |
| B3 | Create a reservation | Select a fictional booker and valid room/time range; successful submission creates one visible reservation. |
| B4 | Inspect reservations | List reservations and inspect their room, booker, time, and status; missing records have an understandable result. |
| B5 | Cancel a reservation | Explicitly confirm cancellation; retained cancelled records are identifiable and release the room for that time. |
| B6 | Seed/reset | Load reproducible fictional rooms, people, and reservations; reset only the documented demo dataset. |
| B7 | Understand state | Loading, empty, invalid input, conflicts, missing data, and service errors are visible. Failed submissions preserve entered values. |
| B8 | Recognize demo purpose | App and README identify demo-only use; fictional identity is not presented as real authentication. |

## Proposed booking rules

- A reservation claims one entire room. Two different rooms may be booked for the
  same time; a room's seat capacity does not permit overlapping reservations.
- Require an existing room/booker and a valid start before end. Set input limits
  and time rules explicitly during refinement, with consistent UI/API validation.
- Proposed intervals are [start, end): a booking ending at 11:00 and another
  starting at 11:00 do not overlap. Cancelled reservations do not block availability.
- Availability is a view of current state, not a guarantee. The authoritative
  create operation must recheck and enforce the conflict rule atomically. Two
  competing requests must not both reserve overlapping time for the same room.
- Disabling a button or checking availability before save is insufficient conflict
  enforcement. Use a proportionate backend/storage mechanism within the approved
  architecture; no distributed locking platform is presumed necessary.
- Make the venue timezone explicit. Agree how ambiguous/nonexistent local times
  are rejected or resolved; never silently rely on the browser/server timezone.
- Cancellation and validation failures must leave a consistent state. Agree the
  outcome of cancelling an already-cancelled reservation and test the chosen rule.
- A transport failure may leave the submission outcome unknown. Do not blindly
  retry and create duplicates; show an honest error and a way to inspect state.

Agreed outcomes (D4–D6): 08:00–20:00 `Europe/London`, 15-minute steps, same-day,
no past starts, 90-day horizon; `[start, end)` per room; repeat cancellation returns
the unchanged record (checked before the start-time rule); an active reservation
cannot be cancelled once started. Tests cover each of these.

## Proposed exclusions

Authentication/roles, real personal data, payments, recurring/group bookings,
waitlists, temporary holds, room administration, booking edits/rescheduling,
notifications, external calendars, analytics, media, real-time updates, multi-venue
timezones, and production deployment are outside the initial scope.

Basic labels, keyboard usability, safe text rendering, readable errors, and correct
core reservation behaviour belong in a usable baseline. Advanced infrastructure,
load testing, and distributed-system exercises do not.

After implementation, distinguish actual supported capabilities from planned ones.
Record an accepted baseline revision only after an authorized commit; do not
automatically promote later exercise solutions into it.
