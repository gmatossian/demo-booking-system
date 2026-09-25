# Delegation brief — build the baseline

## Desired result

A usable local booking application with readable code, fictional fixtures,
repeatable setup/reset, and proportionate verification. It becomes a starting
point for later exercises, not a production booking service.

## First engagement

Inspect the checkout and read the decision register. Present a compact proposal
covering remaining scope, UX, time/booking rules, and persistence choices. Include
the rationale for a backend: reservations require authoritative conflict checking
and shared stored state if that proposed scope is adopted. Angular is requested;
Java/Spring Boot is permitted if needed, not a mandate to add unnecessary layers.

Ask Gabriel about consequential unresolved choices before implementing dependent
work. Do not reopen accepted choices or require a lengthy architecture programme.
A short screen outline and technical outline are enough to make the choices clear.

Once required choices are settled and implementation is authorized, proceed through
implementation, verification, and review preparation without routine confirmation.
Pause for new consequential decisions. Git delivery remains separately authorized.

## Suggested structure

If the proposed backend is adopted, keep both applications in this repository:

    frontend/     Angular app, focused feature components, API access
    backend/      Spring Boot service and build wrapper
    docs/         Capabilities, decisions, contracts, run/check instructions

These directories do not exist yet; this is a suggested layout. Do not introduce
a shared platform with other demo repos or split this system into microservices.

- Choose compatible supported versions using current official Angular, Java, and
  Spring documentation; record prerequisites and resolved versions. Do not inherit
  ExecDesk's tool versions as requirements for this separate application.
- Keep Angular presentation, state, and API access understandable. Avoid a giant
  root component; use separate templates/styles when substantial.
- Keep Java HTTP handling, booking rules, and persistence responsibilities clear.
  Do not hide all behaviour in one controller or create unused abstraction layers.
- Agree the persistence approach and conflict enforcement before establishing the
  schema/API. Document the limits of the chosen single-instance local deployment.
- Document room/booker/reservation fields, IDs, lifecycle, time representation,
  validation, ordering, API responses, and conflict/missing-record behaviour.
- Put the final booking decision in the backend/storage operation, not only the
  Angular client. A preflight availability check cannot reserve capacity.
- Make fictional fixtures deterministic apart from an explicitly documented date
  anchor. Keep them usable as time passes. Record how reset chooses its dates.
- Separate runtime data and generated output from source-controlled fixtures;
  keep local credentials and unrelated user data out of the repository.
- Do not add queues, caches, distributed locks, container orchestration, or paid
  integrations just to make later exercises possible.

## Verification and handoff

Use [acceptance.md](acceptance.md). Focus automated checks on meaningful booking
rules, including a reproducible competing-request case; avoid an exhaustive test
matrix. Demonstrate the main UI path against the real backend if adopted.

Record commands actually run, outcomes, and skipped/blocked checks. Update README
with verified setup/start/stop/reset commands, data location, prerequisites, actual
capabilities, and limits. Do not invent run commands while the app is still absent.

Hand back a reviewable working tree, concise change summary, verification evidence,
decisions made during the work, and known limitations. Human acceptance remains a
separate step; no commit/push/deployment is implied.
