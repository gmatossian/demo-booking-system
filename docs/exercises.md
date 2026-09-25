# Optional later exercises

Ideas only: do not implement these during the baseline build or automatically
create issues. Each can be a learning task or a fictional ExecDesk assignment.

| Scenario | Bounded requirement | Practice area |
| --- | --- | --- |
| Find a suitable room | Filter rooms by capacity/equipment with clear empty results. | Angular forms/state, Java collections and predicates. |
| Change plans | Reschedule a reservation without losing the original if the new time conflicts. | Transaction boundaries, validation, conflict UX. |
| Weekly meeting | Add recurring bookings with an explicit all-or-partial conflict policy. | Date handling, collections, domain rules. |
| Room occupied | Add a waitlist and define promotion when a reservation is cancelled. | Queues, fairness, concurrent updates. |
| Usage report | Summarize reserved hours by room and day. | Java grouping/aggregation, time arithmetic, Angular presentation. |
| Booking reminder | Send simulated local reminders with retry/duplicate rules. | Scheduling, concurrency, idempotency; no real email required. |
| More than one app instance | Design how to preserve booking invariants across service replicas. | System design, database constraints, deployment assumptions. |
| Temporary reservation | Hold a room briefly while a person completes the booking. | Expiry, races, state transitions. |

Advanced concurrency exercises extend the baseline; they do not justify shipping
a baseline that allows double bookings within its supported operation. A design
exercise can finish with a proposal and evidence rather than code.

## Brief for an individual exercise

Specify starting baseline revision, scenario, observable outcome, exclusions,
accepted choices, decisions to explore, expected artifacts/evidence, and human
checkpoints. Choose whether the learner works independently, collaborates, or
delegates; an agent should not automatically solve a learner's exercise for them.

For ExecDesk, use that brief as a fictional issue and select the workflow/modes.
No direct app integration is required. Verify the checkout is suitable before
starting and preserve unrelated work.

Keep experiments in a separate copy or branch when useful. Completion does not
require commits or merging. Review and deliberately keep or discard the result;
never automatically reset unrelated work or promote exercise solutions into the
baseline. Record a stable baseline revision only after acceptance and authorized
Git delivery.
