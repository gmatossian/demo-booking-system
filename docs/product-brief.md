# Booking system — product brief

## Agreed purpose

Build a familiar system with basic working features first. Later, learners or
agents tackle new requirements, bugs, investigations, or design questions against
that baseline. Building the baseline is not itself the learning curriculum.

Practice may be human-led, collaborative, or delegated. ExecDesk dogfooding uses
believable fictional issues to exercise its workflows. The booking app does not
need a direct ExecDesk integration.

Exercise work does not need to be committed or merged into the baseline. Preserve
a useful starting point rather than gradually solving every exercise in it.

## Proposed domain

Meeting-room reservations make the system easy to understand: choose a room and
time, check whether it is free, book it, and cancel if plans change. This avoids
payments and service-provider scheduling in the initial build. Domain and scope
approval are tracked in [decisions.md](decisions.md).

The intended audience is Gabriel, other learners, and people evaluating coding
agents. Use original synthetic data only. Mark the README and app as demo-only;
do not imply this is ready to manage real reservations or personal information.

## Quality and repository

This system has its own repository. Frontend/backend belong together here; do not
create a shared platform across the news-feed, booking, and media-library repos.

Prefer straightforward local setup, understandable behaviour, reproducible demo
data, visible errors, and focused components/services. Verify important rules
without a blanket coverage target or exhaustive platform matrix. Demo status is
not an excuse for tangled code or an unreliable core booking rule.

The repository is private at handoff. Potential future public use is not permission
to publish, deploy, or change visibility. Latest explicit user instructions govern;
record material changes to the accepted direction.
