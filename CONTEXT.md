# Context: OpenFGA Fintech Demo

A Spring Boot demo for a meetup, contrasting JWT-based authorization with OpenFGA.

## The Beat

The single moment this demo exists to produce:

> **Same JWT. The user was fired. The token still works — until OpenFGA. Then the
> *identical* token gets a 403, because reality changed and the system actually asked.**

This is a **freshness / revocation** story, not an expressiveness story. Every feature
in the repo is judged by whether it sharpens "the token is stale but the system still
knows." Anything that doesn't is bonus material, shipped only if time allows.

## Glossary

- **User** — a person who acts in the system (e.g. `user:kwame`). Permissions are never
  assigned to a User directly; they flow through Team membership.
- **Team** — a group of Users (e.g. `team:finance`). The unit of permission assignment.
  Assigning a Team as approver means "every member of this team can approve."
- **Loan** — the top-level approvable object. A Team is assigned as its **approver**.
- **Disbursement** — a payout under a Loan. Has no approver tuples of its own; it
  **inherits** approval authority from its parent Loan.
### Relations

The model carries a realistic set of relations for credibility; the demo's core check
is `can_approve` on a Disbursement. The rest is supporting realism.

**Team**
- **member** — a User belongs to a Team.
- **manager** / **can_manage** — a User manages a Team.

**Loan**
- **borrower** — the User the loan is for.
- **credit_reviewer** / **risk_reviewer** / **approver** — Teams (via `team#member`)
  assigned to review or approve the loan.
- **can_view** — borrower or any reviewer or approver.
- **can_review_credit** / **can_review_risk** / **can_approve** — derived from the
  matching reviewer/approver relation.
- **can_flag** — risk_reviewer or approver.

**Disbursement**
- **parent** — links a Disbursement to its Loan.
- **initiator** / **approver** — Teams assigned directly on the disbursement.
- **can_initiate** — derived from initiator.
- **can_approve** — `approver or can_approve from parent`. The demo exercises the
  `can_approve from parent` arm: no direct approver tuple on the disbursement, so
  approval flows entirely from the parent Loan's approver Team.
- **can_view** — initiator or approver or `can_view from parent`.
- **can_cancel** — approver.

## Revocation

Revocation happens at the **team edge**: deleting the single tuple
`user:kwame member team:finance` cascades through the graph and removes Kwame's
`can_approve` on the Loan and *every* Disbursement under it at once. The demo uses
**one Loan with two Disbursements** to make the cascade visible: both disbursements
pass `can_approve` before revocation and both flip to denied after — though only one
tuple was deleted, and Kwame never had a direct tuple on either disbursement.

Seeding rule that keeps the cascade clean: the two demo Disbursements carry **no direct
`approver` tuple**. The Loan's `approver` is `team:finance#member`; the Disbursements
resolve `can_approve` solely through `can_approve from parent`. So the only path from
Kwame to either Disbursement runs through team membership — cut it and both die.

## The Contrast

Both authorization paths are live in one running app, simultaneously:

- `/jwt/...` approve — guarded by the JWT filter (trusts the token's `role` claim).
- `/fga/...` approve — guarded by a live OpenFGA `check`.

A single **fire** action does both: marks the User inactive in the database *and*
deletes the team-membership tuple in OpenFGA. The climax: fire the User, then hit
both endpoints with the *same* token — `/jwt` returns 200 (stale, the bug),
`/fga` returns 403 (live, the fix). Same identity, same instant, two answers.

## Authentication vs Authorization

The token is never thrown away — it stays valid as **proof of identity**. Both paths
authenticate with it (validate signature, extract `sub`). The difference is authorization:

- **JWT path** also trusts the token's `role` claim — a snapshot frozen at mint time —
  and never asks anything else. The bug is conflating *who you are* with *what you may
  do* and freezing the latter into a token that cannot be un-minted.
- **FGA path** ignores the `role` claim and re-derives authorization live from the graph.

Lesson: the token was never the problem. Using it to answer a question it cannot keep
current is the problem. Authn from the token; authz from OpenFGA.
