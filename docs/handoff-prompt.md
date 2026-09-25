# Handoff prompt

Copy the following into Claude with the repository open:

```text
Work from the root of your demo-booking-system checkout.

Read CLAUDE.md, README.md, and all linked delegation docs before making changes.

Build the initial booking-system baseline as a reusable starting point for later
learning exercises and ExecDesk dogfooding. Use Angular, with Java/Spring Boot
where needed. Do not implement the optional exercises.

First inspect the checkout and decision register. Respect accepted choices and
distinguish them from proposals. Present a compact recommendation for remaining
scope, UX, booking/time rules, and persistence choices, with alternatives where
consequential. Ask me before implementing dependent work. Do not treat silence
as approval or reopen decisions already accepted.

Once those choices are resolved, proceed through implementation, documentation,
and proportionate verification without stopping for routine confirmation. Keep
code readable in focused files. Verify core booking correctness, including the
agreed overlap rule under competing requests, without an exhaustive test matrix.

Stop for new consequential decisions or scope changes; give options and your
recommendation. Do not commit, push, merge, publish, deploy, change visibility,
or create external issues/PRs unless I explicitly authorize it.

Finish with verified run/reset instructions, implemented capabilities, checks
performed, known limitations, and decisions made during the work. Leave the
result ready for my review.
```
