# OpenFGA Fintech Demo — Talk Script

A Spring Boot demo contrasting JWT-based authorization with OpenFGA.
The single moment this demo exists to produce:

> **Same JWT. User fired. Token still works on `/jwt`. Token gets 403 on `/fga`.
> Same identity. Same instant. Two answers.**

---

## Before you start

```bash
# Terminal 1 — OpenFGA
docker compose up -d

# Terminal 2 — Spring Boot
JAVA_HOME=~/.../corretto-21.0.11/Contents/Home ./mvnw spring-boot:run
```

Open Bruno → collection `bruno/` → environment `local`.

Startup logs confirm the graph is live:
```
FGA store created: ...
FGA model written: ...
FGA tuples seeded — user:kwame → team:finance → loan:L1 → D1, D2
```

---

## The setup (say this before request 1)

> "We have a fintech system. Kwame is on the finance team.
> His team is the approver on a loan — and through that loan,
> he can approve two disbursements.
> No direct tuple on either disbursement. It all flows through the graph."

Show the model at `https://play.fga.dev/` — paste `model.fga` and add the four tuples.
Point at the path: `user:kwame → team:finance → loan:L1 → D1, D2`.

---

## Requests 1–4 — everything works

Run 1 → 4. All 200.

> "Both paths work. JWT trusts the role claim in the token.
> FGA asks the graph. Right now they agree."

---

## Request 5 — baseline

Run 5. Show `active: true`.

> "Kwame is active. The DB knows it. The token knows it.
> Everyone agrees."

---

## Request 6 — fire

Run 6. Show `active: false`.

> "Kwame is fired. Two things happened in one request:
> active set to false in the database,
> and the single tuple `user:kwame member team:finance` deleted from OpenFGA.
>
> One deletion at the team edge.
> We didn't touch D1. We didn't touch D2.
> We just cut the path."

---

## Request 7 — the bug

Run 7. Show **200**.

> "Same token. Kwame is fired. JWT says 200.
>
> The token was minted when Kwame was on the finance team.
> That role claim is frozen in the token. It cannot be un-minted.
> The JWT path never asked whether that's still true.
> It just trusted a snapshot from the past."

---

## Request 8 — the fix

Run 8. Show **403**.

> "Same token. Same instant. FGA says 403.
>
> The FGA path ignored the role claim entirely.
> It asked one question: does `user:kwame` have `can_approve`
> on `disbursement:D1` right now?
> The graph said no. The tuple is gone. 403."

---

## Request 9 — the cascade

Run 9. Show **403**.

> "Disbursement B. Also 403.
>
> We never touched D2 directly.
> Kwame never had a direct tuple on D2.
> We deleted one tuple — the team membership —
> and both disbursements fell.
>
> That's the cascade. That's the graph working."

---

## The objection — "just check the database"

> **Audience:** "Why OpenFGA? Can't you just check `active = false` in the DB?"

> **Answer:** "You can — for this case. Check the flag, deny the request. Done.
>
> But `active` is a property of a user.
> Authorization is a question about a relationship between a user, an action, and a resource.
>
> What happens when you need to ask: can this user approve *this specific disbursement*?
> That's not a field on a row. That's a path through a graph.
>
> What happens when a team changes approvers mid-process?
> When a loan is transferred? When you need to audit *why* someone had access?
>
> The database flag works until the relationships get complex.
> OpenFGA scales with the relationships — not against them."

---

## The epilogue — "so I have to wire this manually every time?"

> **Answer:** "No. We wired it by hand deliberately — so you could see every call.
> The `AuthorizationService.check(...)` is three lines. The graph query is explicit.
>
> In production you'd use the Spring Boot starter:"

```java
@PreAuthorize("@fga.check('disbursement', #id, 'can_approve', 'user')")
public ResponseEntity<ApproveResponseDto> approve(@PathVariable String id) { ... }
```

> "One annotation. The explicit call becomes invisible.
> We chose to keep it visible today so the story was clear."

See `docs/adr/0002-raw-sdk-not-spring-boot-starter.md` for the full reasoning.

---

## Key files

| File | What it shows |
|---|---|
| `src/.../service/JwtService.java` | Token minting — role frozen at mint time |
| `src/.../controller/PayoutController.java` | Both paths side by side — JWT vs FGA |
| `src/.../service/AuthorizationService.java` | The live FGA check — three lines |
| `src/.../service/FgaBootstrap.java` | Graph seeded on startup — the tuples |
| `src/.../controller/AdminController.java` | Fire — DB + FGA in one action |
| `src/main/resources/fga/model.fga` | The authorization model in DSL |
| `docs/adr/` | Why these decisions were made |
