# Build Steps — OpenFGA Fintech Demo (Spring Boot)

> See `CONTEXT.md` for the design and `docs/adr/` for the key decisions.

---

## Status

| Step | What | Done |
|---|---|---|
| 1 | Generate project at start.spring.io | ✅ |
| 2 | Extract into repo, verify existing files survived | ✅ |
| 3 | Add OpenFGA SDK + jjwt to `pom.xml` | ✅ |
| 4 | Docker Compose for OpenFGA | ✅ |
| 5 | Package layout created | ✅ |
| 6a | Entities, DTOs, exceptions, repositories, seed data | ✅ |
| 6b | JWT mint + login endpoint | ✅ |
| 6c | JWT authentication filter | ✅ |
| 6d | JWT-guarded approve endpoint (the bug) | ✅ |
| 6e | OpenFGA client `@Bean` | ✅ |
| 6f | Bootstrap — create store, write model, seed tuples | ✅ |
| 6g | FGA-guarded approve endpoint (the fix) | ✅ |
| 6h | Fire endpoint | ✅ |
| 6i | Walk the 9-request spine | ✅ |
| 6j | Bruno collection + README talk-script | ✅ |

---

## Final file layout

```
src/main/java/com/example/fintech_demo/
├── model/
│   ├── User.java                  ✅  @Entity — id (String), name, role, active
│   ├── Loan.java                  ✅  @Entity — id, status (Status enum), amount
│   ├── Disbursement.java          ✅  @Entity — id, status, amount, loanId (String FK)
│   ├── Role.java                  ✅  enum — USER, ADMIN, FINANCE
│   └── Status.java                ✅  enum — INITIATED, PENDING, COMPLETED
├── dto/
│   ├── LoginUserDto.java          ✅  record { userId }
│   ├── LoginResponseDto.java      ✅  record { token }
│   ├── ApproveResponseDto.java    ✅  record { disbursementId, approvedBy, message }
│   ├── UserResponseDto.java       ✅  record { id, name, role, active }
│   └── ErrorResponseDto.java      ✅  record { timestamp, status, error, message, path }
├── exception/
│   ├── ResourceNotFoundException.java      ✅  → 404
│   ├── UserNotFoundException.java          ✅  → 404
│   ├── LoanNotFoundException.java          ✅  → 404
│   ├── DisbursementNotFoundException.java  ✅  → 404
│   ├── ForbiddenException.java             ✅  → 403
│   └── GlobalExceptionHandler.java         ✅  @RestControllerAdvice
├── repository/
│   ├── UserRepository.java        ✅  JpaRepository<User, String>
│   ├── LoanRepository.java        ✅  JpaRepository<Loan, String>
│   └── DisbursementRepository.java✅  JpaRepository<Disbursement, String>
├── config/
│   ├── SecurityConfig.java        ✅  stateless, JwtFilter wired, /auth/** + /h2-console/** permitted
│   ├── OpenFgaConfig.java         ✅  OpenFgaClient @Bean — URL from app.openfga.api-url
│   └── BeansConfig.java           ✅  AuthenticationProvider, PasswordEncoder, CorsFilter
├── security/
│   └── JwtFilter.java             ✅  OncePerRequestFilter — validates token, sets SecurityContext
├── service/
│   ├── JwtService.java            ✅  generateToken, extractUserId, isTokenValid (jjwt 0.12)
│   ├── AuthService.java           ✅  login(userId) → JWT
│   ├── UserServiceImpl.java       ✅  UserDetailsService — loadUserByUsername via findById
│   ├── AuthorizationService.java  ✅  check(userId, relation, object) → live OpenFGA check
│   └── FgaBootstrap.java          ✅  CommandLineRunner — store → model → tuples on startup
└── controller/
    ├── AuthController.java        ✅  POST /auth/login
    ├── PayoutController.java      ✅  POST /jwt/payouts/{id}/approve (the bug)
    │                                  POST /fga/payouts/{id}/approve (the fix)
    └── AdminController.java       ✅  GET  /admin/users/{id}
                                       POST /admin/fire/{userId}

src/main/resources/
├── application.yaml    ✅  H2, JPA, JWT secret, OpenFGA URL, logging
├── data.sql            ✅  seeds kwame / L1 / D1 / D2
└── fga/
    ├── model.fga       ✅  authorization model DSL
    └── model.json      ✅  converted JSON (fga model transform) — used by FgaBootstrap

docker-compose.yml      ✅  OpenFGA on 8081 (HTTP) + 3000 (playground)
bruno/                  ✅  9-request collection, local environment, token auto-capture
README.md               ✅  talk-script with objection beats and epilogue
```

---

## Running the demo

```bash
# 1. Start OpenFGA
docker compose up -d

# 2. Start the app
JAVA_HOME=/Users/jessemurah/Library/Java/JavaVirtualMachines/corretto-21.0.11/Contents/Home \
  ./mvnw spring-boot:run
```

Startup confirms the graph is live:
```
FGA store created: ...
FGA model written: ...
FGA tuples seeded — user:kwame → team:finance → loan:L1 → D1, D2
```

Open Bruno → collection `bruno/` → environment `local` → run 1 → 9.

---

## The 9-request spine

| # | Method | URL | Expected | Why |
|---|---|---|---|---|
| 1 | POST | `/auth/login` | 200 + token | |
| 2 | POST | `/jwt/payouts/D1/approve` | 200 | JWT path works |
| 3 | POST | `/fga/payouts/D1/approve` | 200 | FGA path works |
| 4 | POST | `/fga/payouts/D2/approve` | 200 | cascade works both ways |
| 5 | GET  | `/admin/users/kwame` | `active: true` | |
| 6 | POST | `/admin/fire/kwame` | 200 | DB + FGA in one action |
| 7 | POST | `/jwt/payouts/D1/approve` | **200** | stale token — **the bug** |
| 8 | POST | `/fga/payouts/D1/approve` | **403** | live graph — **the fix** |
| 9 | POST | `/fga/payouts/D2/approve` | **403** | one deletion, both dead — **the cascade** |

---

## The demo beat

> Same JWT. User fired. Token still works on `/jwt`. Token gets 403 on `/fga`.
> Same identity. Same instant. Two answers.
> One deletion cascaded to both disbursements — no direct tuple on either one.
