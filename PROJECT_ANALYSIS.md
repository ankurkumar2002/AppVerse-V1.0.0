# AppVerse — Project and Architecture Handoff

This document is a code-oriented guide for an AI assistant or developer joining the AppVerse repository. It explains the current structure and the responsibilities inferred from the checked-in source. Treat source code and service configuration as authoritative when they disagree with older descriptions or diagrams.

## 1. What the product does

AppVerse is a marketplace for software applications. It has two main authenticated personas:

- **Developers** complete a developer profile, publish and maintain app listings, and configure subscription plans.
- **Users** complete a user profile, browse listings, inspect app details, maintain a cart, and view orders. Subscription and payment backend services are also present.

Public pages include the landing page, About, and Contact. The application is a single-page Angular frontend that sends REST requests through the API Gateway. Keycloak handles login and supplies JWT identity/role claims. The backend is divided into Spring Boot services, with MongoDB for app catalog records and MySQL/Flyway for most transactional records. Kafka is used for selected asynchronous domain events.

This is an active development codebase. Do not assume every feature described in prose is complete: check its controller, service implementation, route, and frontend caller before making a claim or extending it.

## 2. Repository map

```text
AppVerse - V1.0.0/
├── frontend/appverse/       Angular 19 client
├── api-gateway/             Public API entry point, routing, security, resilience
├── app-service/             Application catalog, categories, image upload, MongoDB
├── developer-service/       Developer profiles and developer-related operations
├── user-service/            Marketplace user profiles
├── identity-service/        Keycloak identity/user operations
├── cart-service/            User cart and cart items
├── order-service/           Orders and order items
├── payment-service/         Payment transactions and saved payment methods
├── subscription-service/    Plans and user subscriptions
├── notification-service/    Kafka consumers and email notifications
├── uploads/                 Repository-level upload/media area (also check app-service)
├── README.md                Setup notes and endpoint overview
└── PROJECT_ANALYSIS.md      This handoff
```

Each Java backend directory is an independent Maven/Spring Boot application, generally with its own `pom.xml`, Maven wrapper, `src/main/java`, `src/main/resources`, and tests. The common Java package structure follows the service name, then layers such as `controller`, `service`, `repository`, `model`, `dto`, `mapper`, `config`, `client`, and `exception`. Not every service has every layer. SQL schema changes live in that service's `src/main/resources/db/migration` and are applied by Flyway. Docker Compose and Docker support files exist in several service directories and under `api-gateway/docker`; there is not one obvious root-level compose file in the repository root.

## 3. Frontend (Angular)

### Technology and entry points

The Angular app is in `frontend/appverse`. `package.json` identifies Angular 19.2.18, TypeScript 5.8, RxJS, Keycloak JS, Angular Material, PrimeNG, Bootstrap, and Angular SSR/Express dependencies. `npm start`/`pnpm start` runs `ng serve`; the app is configured around localhost services rather than an environment-specific API config.

- `src/main.ts`: browser bootstrap.
- `src/app/app.config.ts`: application providers (router, HTTP, auth initialization, and related global services; inspect this when wiring a new app-level provider).
- `src/app/app.routes.ts`: authoritative route table and guards.
- `src/app/app.component.*`: root shell.
- `src/app/core/auth/`: Keycloak initialization and HTTP bearer-token interceptor.
- `src/app/core/guards/`: authentication, role, and profile-completion route checks.
- `src/app/core/services/`: cross-feature auth, category, and subscription-plan API helpers.
- `src/app/features/`: business features grouped by domain.
- `src/app/layout/`: developer and user authenticated shells/navigation.
- `src/app/pages/`: public landing, about, and contact pages.
- `src/styles.scss`: global dark theme tokens, reset, and global overlay styling.
- Feature components usually have `.component.ts` for behavior, `.component.html` for structure, and `.component.scss` for local visuals. Many have adjacent `.spec.ts` files.

### Feature domains

- `features/application/`: developer-side application listing/create/detail/edit and the application API service/models. This is the publisher workflow.
- `features/developer/`: profile onboarding/editing, dashboard, personal details/password, and developer API client/models.
- `features/user/`: profile completion, user dashboard, catalog list/detail, account editing, cart view, and user-related API code.
- `features/cart/`: cart page, item, summary, service, and request/response models.
- `features/order/`: order list/history, service, and order DTO/enums.
- `features/shared/`: small cross-feature utilities such as password matching validation.

The route table currently defines:

- Public: `/landing`, `/about`, `/contact` (empty path redirects to landing).
- Developer shell: `/developer`, with profile create/update, dashboard, personal details/password, and `/developer/apps` list/create/detail/edit. Authentication is required for the shell; role checks protect the dashboard and app routes. Onboarding routes intentionally accept a signed-in user before the DEVELOPER role/profile flow is complete.
- User shell: `/user`, with dashboard, profile completion, app browse/detail, profile/edit/password, cart, and orders. Authentication is required for the shell; role checks protect most user pages and a profile guard participates in profile completion.

The Angular route guards improve navigation and UX. Backend gateway/service authorization remains the security boundary; do not treat a frontend guard as sufficient authorization.

### Frontend API and auth conventions

Most API clients use explicit `http://localhost:9000/...` URLs (gateway port 9000). Keycloak is configured at `http://localhost:8181`, realm `appverse`. Feature services call gateway paths such as `/api/apps`, `/api/developers`, `/api/v1/users`, `/api/v1/carts`, and `/api/v1/orders`. The HTTP interceptor is responsible for attaching the current access token; check its implementation and provider registration when adding calls. Some paths are constructed directly inside components, so search the feature before assuming every call lives in a service.

Typescript models mirror API request/response shapes but are manually maintained. When changing an endpoint, update the backend DTO/controller, frontend model, service call, and consuming component together. Watch naming and enum differences across Java and TypeScript.

### Visual theme

`src/styles.scss` defines a dark interface: near-black page background (`#090a0f`), layered charcoal surfaces, subtle white borders, light primary text, muted secondary text, purple accent (`#7c5cff`), green success, and red error. The global font stack prefers Inter, then system sans-serif. It defines layout tokens for 250px sidebar, collapsed 76px sidebar, and 70px toolbar. It also overrides Angular Material and PrimeNG select/menu overlay surfaces so popups remain opaque and readable against the dark background.

When adding UI, follow the relevant layout and neighboring component SCSS rather than inventing a new palette. Global tokens are the shared baseline; many components also have substantial local styling, so inspect their HTML and SCSS for the actual page treatment. Material, PrimeNG, Bootstrap, and hand-written CSS coexist; use the library already used by the surrounding component where practical.

## 4. Backend services and their roles

All services are Spring Boot applications. Most expose REST controllers and use Spring Security as OAuth2 resource servers validating Keycloak JWTs. Internal HTTP clients commonly use OpenFeign/WebClient plus a service-token provider/interceptor. Controllers are the HTTP contract; services contain business logic; repositories own persistence; DTOs separate API representations from persistence models; mappers convert between them; exception handlers translate domain and persistence errors.

| Service | Main responsibility | Persistence / integration |
|---|---|---|
| `api-gateway` | Public entry point on port 9000; routes paths to internal services, applies route authorization, CORS, JWT/security and resilience policies, exposes aggregated OpenAPI routes. | No business database. Route definitions are in `routes/Routes.java`; policy/config helpers live under `config/` and `routes/HelperMethods/`. |
| `app-service` | Application catalog and categories, listing lifecycle, screenshot/media handling. | MongoDB (`app-service` database by default), Kafka producer, multipart upload directory. Main config is `app-service/src/main/resources/application.properties`. |
| `developer-service` | Developer profile lifecycle and developer account-related API. | MySQL/Flyway; communicates with identity/user services as needed; publishes developer events in code. |
| `user-service` | User profile creation, lookup, updates, and related user operations. | MySQL/Flyway; Keycloak identity integration. |
| `identity-service` | Keycloak-facing identity operations such as current identity and user/role administration. | Keycloak Admin/API integration; no app-owned relational domain database apparent from its purpose. |
| `cart-service` | Per-user cart and item quantity management. | MySQL/Flyway; application-service client for app details; cart event publisher. |
| `order-service` | Order creation, order state, and order history. | MySQL/Flyway; clients to app/subscription/payment-related APIs and order events. |
| `payment-service` | Payment transaction initiation/status and stored payment methods. | MySQL/Flyway. A webhook/mock update route is not evidence of a production payment provider integration; inspect gateway implementation before extending payment flows. |
| `subscription-service` | Subscription plans and user subscription lifecycle. | MySQL/Flyway; payment-service client; event payload classes exist, although README notes some subscription publishing is commented out. |
| `notification-service` | Consumes selected Kafka events and sends email using templates. | Kafka and SMTP/email configuration; it is event consumer oriented rather than a user-facing CRUD service. |

Common backend locations:

- `controller/`: REST paths, parameters, validation, and response status.
- `service/` and `service/serviceImpl/` (or similar): business logic.
- `repository/`: Spring Data/JPA or Mongo repositories.
- `model/`: persisted entities/documents.
- `dto/`: request/response wire contracts.
- `mapper/`: DTO/model transformation, often MapStruct.
- `client/`: downstream service clients and service-to-service auth.
- `config/`: security, Kafka, cache, observation, client, and framework setup.
- `exception/`: domain exceptions and `GlobalExceptionHandler` response mapping.
- `event/`, `publisher/`, `consumer/`: Kafka messages and event boundaries.
- `src/main/resources/application.properties`: ports, DB URLs, Keycloak issuer, broker, upload locations, observability, and client settings.
- `src/main/resources/db/migration/`: versioned Flyway schema migrations.
- `src/test/`: unit/integration tests. `app-service` includes Testcontainers-oriented tests; `cart-service` has controller, repository, and service tests.

## 5. Request and event flow

### Synchronous request path

```text
Angular component -> feature API service (HTTP + bearer token)
 -> API Gateway :9000 (JWT/route policy/resilience)
 -> selected Spring service controller
 -> service/business logic -> repository/database
 <- DTO/HTTP response returns through gateway to Angular
```

Gateway route mappings are explicitly declared in `api-gateway/.../routes/Routes.java`. Current mappings include apps/categories to `app-service`, developers to `developer-service`, users to `user-service`, carts to `cart-service`, orders to `order-service`, payments to a configured downstream port, and subscriptions/plans to subscription service. Exact paths, downstream ports, and `RoutePolicy` differ by route; consult this file and `application.properties` before adding a client route. For example, the checked-in gateway route source maps `/api/v1/payments/**` to `localhost:8085`, while some older prose may list a different payment port. Resolve such discrepancies from the service's active config and compose setup before running the full stack.

### Representative user journeys

1. **Sign-in and onboarding:** Angular initializes Keycloak; after authentication, the developer or user completes a profile through the corresponding profile endpoint. Profile creation routes have special gateway policies because the account may not yet have its final marketplace role/profile.
2. **Developer publishes an app:** developer UI submits listing data and media to app-service through gateway; app-service validates/stores metadata in MongoDB and files in the configured upload directory. It may publish an application event to Kafka; notification-service consumes supported events and sends email.
3. **User browses and purchases:** user catalog/detail pages use app APIs. Cart and order APIs are separate services and persist their own records. Payment initiation is another service boundary. Follow actual controller and client calls for state transitions; do not assume a distributed transaction across services.
4. **Subscription:** subscription-plan and user-subscription APIs are handled by subscription-service, with payment-service calls for payment work. Event payloads exist, but check whether a given event publisher is currently enabled before relying on it.

### Kafka

The code/README identify topics including `application-events`, `category-events`, `developer-events`, `cart-events`, `order-events`, and `payment-events`. The documented consumer is notification-service, including application events; topic declarations and actual listeners determine what is active. Kafka messages decouple side effects such as notifications from the REST request. Check event classes, producer, serializer/deserializer configuration, and consumer group together when changing a payload.

## 6. Data and configuration

- `app-service` uses MongoDB because app catalog records contain flexible listing data and screenshot references.
- Developer, user, cart, order, payment, and subscription services use MySQL with service-local Flyway migrations (verify each current `application.properties`). Databases are service-owned logically even if local compose files share one MySQL server.
- Keycloak issuer is typically `http://localhost:8181/realms/appverse`; gateway is `http://localhost:9000`; Angular dev server is `http://localhost:4200`; MongoDB defaults to 27017; Kafka commonly uses 9092. Service ports should be taken from each service's properties and compose configuration, not copied from stale diagrams.
- Configuration currently includes machine-specific localhost URLs and may include credentials/client secrets. Do not copy secret values into documentation, prompts, commits, or logs. Prefer environment variables/secrets management when changing deployment configuration.
- `app-service` sets a Windows-specific upload path in its properties. The API and filesystem path must agree, and uploaded files are distinct from Mongo document records.
- `management.*`, Micrometer/tracing, and Resilience4j settings provide health/metrics/circuit breaker behavior where enabled. Gateway also has rate-limit/policy-related classes; inspect their wiring before assuming every policy is active.

## 7. Build and local development

README setup assumes infrastructure is available locally: Keycloak realm/clients, MySQL databases, MongoDB, and Kafka. Start the infrastructure first, then individual backend services using their Maven wrapper (`mvnw.cmd spring-boot:run` on Windows), and finally the Angular app from `frontend/appverse` (`npm install`/`pnpm install`, then `npm start`/`pnpm start`). The frontend expects gateway on port 9000 and Keycloak on 8181. API documentation is available from the gateway Swagger UI at `/swagger-ui.html` when the gateway and target services are running; individual Spring services also have their own OpenAPI endpoints.

Useful checks before changing behavior:

1. Read `README.md` for local setup and endpoint overview.
2. Trace the Angular route -> component -> service -> TypeScript model.
3. Trace the gateway path and policy in `api-gateway/.../Routes.java` and `RoutePolicy`.
4. Trace the target controller -> service -> repository/model/DTO -> migration.
5. Check downstream clients, Kafka producer/consumer, and config if the flow crosses services.
6. Update tests and any request/response types affected by the contract change.

## 8. Guidance for AI-assisted development

When asked to change this project, first identify the exact user journey and owning service. Keep changes inside the existing service/domain boundaries unless there is a clear reason to change architecture. Preserve API compatibility unless the task explicitly permits a contract change. For a new endpoint, usually update controller, service interface/implementation, DTO/validation, persistence/migration if needed, gateway route/security policy, Angular model/service/component, and tests as applicable. Avoid adding a direct frontend call to a backend service when the established flow uses the gateway.

Before relying on a feature claim, verify the implementation. In particular, this repository has multiple services and evolving integrations: documentation may be aspirational or stale, route ports can disagree, some event publishers may be disabled, and a mock/webhook endpoint does not prove a live provider integration. Explain uncertainty instead of filling gaps with assumptions. Never expose secrets from local properties files.

## 9. Source-of-truth and known caveats

This handoff summarizes the repository structure and selected configuration as checked in, but it is not a substitute for tracing a specific feature end to end. `README.md` is useful for setup and endpoint discovery; code is authoritative for implementation. Port maps and feature claims have shown inconsistencies between prose and code. The service count is nine domain/identity services plus the gateway. The Angular app includes SSR dependencies, but its primary application structure is a client-side routed SPA; do not assume the server-rendered deployment path is the standard local workflow.
