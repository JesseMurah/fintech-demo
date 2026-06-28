# 1. Bootstrap OpenFGA store, model, and tuples on application startup

Date: 2026-06-16

## Status

Accepted

## Context

The demo needs an OpenFGA store, an authorization model, and a seed set of
relationship tuples to exist before any check can run. The OpenFGA Playground at
`localhost:3000` lets you create all of this by hand through a visual editor, and the
graph view it provides is genuinely compelling for an audience.

But this is a live meetup demo. Hand-created state is fragile on stage: if the OpenFGA
container restarts, the machine is fresh, or the datastore volume is wiped, the store ID
is gone — and every saved REST-client request that hard-codes that store ID breaks. The
visual model-authoring step is also slow and error-prone to perform live.

The Python `fga_example` this demo descends from bootstraps programmatically: create
store, write the model from a checked-in `model.fga`, seed tuples, print the IDs.

## Decision

Bootstrap programmatically on application startup. A startup component checks for an
existing store and, if absent, creates the `fintech-demo` store, writes the model read
from a checked-in `model.fga`, and seeds the relationship tuples. The returned store and
model IDs are fed directly into the `OpenFgaClient` configuration — never hand-copied.

The model lives as a version-controlled `model.fga` in the repo, read at bootstrap, so
it is reviewable rather than buried in Java string literals.

The Playground is kept as a **read-only visual aid** — opened during the talk to show
the model and the relationship graph, never to author state.

## Consequences

- `docker compose up` + running the app yields a deterministic, restart-surviving setup
  every time. Saved REST-client requests target a store the app guarantees exists.
- More Java code than manual setup, and the model-writing step is hidden from the
  audience — mitigated by showing the model in the Playground as a visual.
- The model is version-controlled and diff-reviewable.
