# 2. Use the raw OpenFGA Java SDK, not the Spring Boot starter

Date: 2026-06-16

## Status

Accepted

## Context

OpenFGA publishes an official `dev.openfga:openfga-spring-boot-starter` (0.3.1, Java 17+,
Spring Boot 3+). It auto-configures an `OpenFgaClient` bean from `openfga.*` properties and
provides declarative, annotation-based checks:

```java
@PreAuthorize("@fga.check('disbursement', #id, 'can_approve', 'user')")
```

A future reader will reasonably ask why this demo wires the raw SDK by hand instead of
adopting the official starter.

## Decision

Use the raw `dev.openfga:openfga-sdk` directly, behind an explicit `AuthorizationService`.
Do not adopt the Spring Boot starter for the core demo. Note the starter in the README as
the "production epilogue."

## Consequences

Three reasons drive this, all rooted in the demo's freshness beat (see CONTEXT.md):

1. **Transparency over magic.** The demo's whole point is "every request asks the graph,
   right now." That call must be *visible* — a plain line of Java the audience can read,
   with the user/relation/object and the boolean result on screen. The starter's headline
   feature, `@PreAuthorize("@fga.check(...)")`, hides exactly that mechanic behind a SpEL
   string. We deliberately want the explicit `AuthorizationService.check(...)`.

2. **The starter doesn't help with setup.** It only runs checks against a pre-existing
   store; it has no store/model creation helpers. Our startup bootstrap (ADR-0001) is
   needed either way, so the starter adds nothing on the setup side.

3. **Static config fights create-on-startup.** The starter binds `store-id` /
   `authorization-model-id` at context-init, but ADR-0001 *creates* the store on startup,
   so those IDs don't exist yet. The raw SDK's `createStore()` →
   `setStoreId()` → `writeAuthorizationModel()` → `setAuthorizationModelId()` on one client
   is precisely the lifecycle our bootstrap needs; the starter takes that control away.

Trade-off accepted: we hand-wire a small `OpenFgaClient` `@Bean` (~12 lines) instead of
getting it auto-configured, and we forgo the annotation ergonomics. The README shows the
`@PreAuthorize("@fga.check(...)")` one-liner as the idiomatic production approach — turning
"why not the starter?" into a closing teaching beat rather than an unanswered question.
