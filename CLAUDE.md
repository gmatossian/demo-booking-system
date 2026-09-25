# Working on demo-booking-system

Read README.md, docs/product-brief.md, docs/decisions.md,
docs/baseline-spec.md, docs/implementation-brief.md, and docs/acceptance.md first.
Read docs/exercises.md for the boundary between baseline work and future tasks.

Build a small working baseline for later learning exercises and ExecDesk
dogfooding. These documents do not imply that proposals are accepted or features
already exist. Your current user message determines the authorized engagement.

## Working agreement

- Check the decision register before coding. Bundle related open choices into a
  compact recommendation with alternatives and ask Gabriel. Do not treat silence
  as approval or reopen accepted choices without a concrete reason.
- Once implementation is authorized and required choices are settled, carry out
  routine edits, documentation, and proportionate verification without repeatedly
  asking to continue. Continue independent preparation while awaiting answers.
- Stop for new consequential product, UX/design, architecture, dependency, data,
  privacy, or scope choices. Present the issue, options, and recommendation.
- Keep code readable in focused files. Avoid both giant components/controllers
  and abstractions or infrastructure that the baseline does not need.
- Do not implement the optional exercises. Basic booking correctness must not be
  deferred merely because advanced concurrency is a future learning topic.

## Boundaries

- Work only in this repository; no changes to ExecDesk or sibling demo systems.
- Human scope/design decisions and final acceptance remain Gabriel's unless
  explicitly delegated.
- No commits, pushes, merges, tags, external issues/PRs, deployment, publishing,
  or repository visibility changes without explicit project-specific authority.
- No paid services, credential acquisition, or external runtime integrations
  without authorization. Use fictional rooms, people, and reservations.
- A fictional booker selector is not authentication or access control.
- Do not discard unrelated changes or reset existing data as incidental cleanup.

## Completion

Report what works, how to run/reset it, actual verification, known limitations,
decisions made during the work, and what remains for human review. Leave a
reviewable working tree. Implementation complete, accepted, and delivered through
Git are separate states.

## Preserve the baseline during exercises

Before changing files for an exercise, inspect the current branch and working tree.
Use a separate exercise branch; if the checkout is on main, create or switch to an
exercise branch before editing. Preserve unrelated changes. Do not merge exercise
solutions into main automatically. A completed exercise may remain uncommitted or
unmerged. Baseline maintenance and promotion of exercise work require explicit
authorization; commit/push authority is still governed by the boundaries above.
