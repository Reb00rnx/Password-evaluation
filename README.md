# PasswordGym

A small Spring Boot microservice that evaluates password strength. Built for a client context where the platform must satisfy HIPAA/FedRAMP-style expectations around password security.

## Run it

```bash
./mvnw spring-boot:run
```

Or with Docker:

```bash
docker build -t passwordgym .
docker run -p 8080:8080 passwordgym
```

Runs on port `8080`.

## Test it

```bash
./mvnw test
```

23 tests: `PasswordGymServiceTest` (19) - pure unit tests of the scoring logic, no Spring context. `PasswordGymControllerTest` (4) - `@WebMvcTest` slice covering the HTTP layer and input validation.

## API

`POST /api/v1/password/evaluate`

Request:
```json
{ "username": "okenobi", "email": "o.kenobi@jedi-council.com", "password": "Hello there!" }
```

Response:
```json
{
  "score": 10,
  "strength": "STRONG",
  "meetsPolicy": true,
  "containsUsernameOrEmail": false,
  "containsSpecialCharacter": true,
  "containsUpperCase": true,
  "containsDigit": false,
  "message": ["Consider adding a digit for a stronger password"],
  "isCommonPassword": false
}
```

### Request fields

| Field | Type | Description |
|---|---|---|
| `username` | `String` | Required, non-blank. Checked against the password (case-insensitive) to catch identity-based passwords. |
| `email` | `String` | Required, non-blank, valid email format. The local part (before `@`) is checked against the password the same way as `username`. |
| `password` | `String` | Required, non-blank, max 128 characters. The password being evaluated. |

### Response fields

| Field | Type | Description |
|---|---|---|
| `score` | `int` | Numeric score from length tier + character-composition bonus. `0` whenever a hard policy gate fails. |
| `strength` | `String` enum | `VERY_WEAK` / `WEAK` / `FAIR` / `GOOD` / `STRONG` - the categorical strength level. |
| `meetsPolicy` | `boolean` | `true` only if the password passes every hard rule (length, not a common password, doesn't contain identity, no long repeated/sequential runs). |
| `containsUsernameOrEmail` | `boolean` | Whether the password contains the username or the email's local part. |
| `containsSpecialCharacter` | `boolean` | Whether the password contains at least one non-alphanumeric character. |
| `containsUpperCase` | `boolean` | Whether the password contains at least one uppercase letter. |
| `containsDigit` | `boolean` | Whether the password contains at least one digit. |
| `message` | `List<String>` | Concrete feedback: violations of hard rules and suggestions for missing character classes. |
| `isCommonPassword` | `boolean` | Whether the password (or its base with a trailing suffix stripped) matched the common-password list. |

Swagger UI: `/swagger-ui/index.html`. Health: `/actuator/health`.

## Key decisions

**No mandatory composition rules.** NIST SP 800-63B (which FedRAMP references via NIST SP 800-53 IA-5) explicitly advises against requiring uppercase/digit/symbol composition - it's still common in practice, but that's legacy convention, not current guidance for a security-conscious client. Character diversity is a **bonus** (+1 point each, capped), never a requirement, so a long passphrase with no digits or symbols can still score highly - length is the dominant factor, per NIST's preference for long passphrases over short, complex passwords.

**Length thresholds - 10 / 12 / 16.** NIST sets 8 as an absolute floor, 12+ "highly recommended", 16+ for admin accounts. 10 was chosen as a deliberately stricter hard minimum than NIST's bare floor for this client profile; 12 and 16 are non-blocking scoring tier boundaries above that. All three are externalized to `application.properties` (`password.policy.*-length`) rather than hardcoded, so they're tunable per environment without a rebuild.

**Hard gates vs. soft penalties.** Below-minimum length, presence on the common-password list, and containing the username/email are treated as absolute gates. They force `meetsPolicy: false` and cap `strength` at `WEAK`/`VERY_WEAK` regardless of score - no amount of length or variety should let a known-breached or trivially-guessable password read as "strong". Repeated/sequential runs (`aaa`, `123`) are a softer signal and don't block on their own. The username/email check is skipped when the username or email's local part is shorter than 3 characters, to avoid flagging a password for coincidentally containing an unrelated short substring rather than the user's actual identity.

**Common password list** (`~100k` entries, `top_most_used.txt`, loaded once into a `Set` at startup) matches case-insensitively and also strips trailing non-letter characters before comparing, to catch patterns like `Starwars1!`. This is a simplified implementation covering the most basic requirements and the most trivial cases (exact matches and simple appended-suffix variants). A natural extension would be a library like [zxcvbn](https://github.com/dropbox/zxcvbn), which does full pattern matching (dictionary words, l33t-speak, keyboard-adjacency).

**Response never echoes the password, username, or email back.** No persistence, no logging of the raw request anywhere - the service is stateless.

## Production readiness

Beyond the core endpoint:

- **Docker** - multi-stage `Dockerfile` producing a small JRE-only runtime image (JDK/Maven never ship in the final image).
- **Health check** - `/actuator/health`, suitable for a container orchestrator's liveness/readiness probes.
- **Self-documenting API** - Swagger UI / OpenAPI at `/swagger-ui/index.html` and `/v3/api-docs`.
- **Externalized configuration** - password length thresholds live in `application.properties`, not hardcoded, so they can be retuned per environment without a rebuild.
- **Stateless design** - no database, no session state, so the service scales horizontally without any coordination between instances.

### Further considerations for a production deployment

Not implemented here, but worth naming given the regulated-industry (HIPAA/FedRAMP) context this service is built for:

- **Audit logging** - if integrated into a larger system, evaluation attempts (never the password itself) would need to be logged to satisfy HIPAA-style audit trail requirements.
- **Dependency vulnerability scanning** (e.g. OWASP Dependency-Check, Snyk) as part of the build pipeline.
- **CORS configuration** - if a browser-based frontend on a different origin consumes this API, it needs an explicit allow-list, never a wildcard origin.
- **Observability beyond `/actuator/health`** - e.g. Micrometer metrics on evaluation outcomes, to see strength-distribution trends in production rather than just service liveness.

## Known limitations

- No rate limiting on the endpoint; a production deployment would add it (e.g. Bucket4j, or at the gateway layer) to stop the endpoint being used as a password-guessing oracle.
- No dictionary/l33t-speak detection beyond the common-password list (see zxcvbn note above).
- HTTPS is assumed to be terminated by infrastructure in front of this service.

## Stack

Java 21, Spring Boot 4, Maven.
