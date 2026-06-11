# 📱 AppVerse — Application Marketplace Platform

> A modern, microservices-driven platform where developers publish applications and users discover, subscribe to, and purchase digital solutions with seamless payment integration.

---

## 🎯 Overview

**AppVerse** is a full-stack, production-grade application marketplace ecosystem built to democratize software distribution. It empowers independent developers and organizations to monetize their creations through a flexible, secure platform that handles everything from identity management and payment processing to subscription billing and real-time notifications. Whether users are looking to discover free or premium applications, or developers are launching the next breakthrough software product, AppVerse provides the infrastructure, tooling, and user experience to make it happen at scale.

The platform operates on a dual-role architecture: **developers** manage application listings, configure pricing models (free, one-time purchase, subscription-based, or hybrid), and track revenue; **users** browse curated application catalogs, manage subscriptions, and execute purchases with industry-standard payment methods. Behind the scenes, a carefully orchestrated ecosystem of microservices handles authentication, payment processing, cart management, order fulfillment, subscription lifecycle, and event-driven notifications—all secured by OAuth 2.0 with Keycloak.

What makes AppVerse technically impressive is its event-driven async architecture using Apache Kafka for decoupled services, resilience patterns with circuit breakers and load balancing, comprehensive API aggregation through a Spring Cloud Gateway, and role-based access control that enforces granular permissions across developer and user workflows. The frontend delivers a modern, responsive single-page application built with Angular 19 and standalone components, while the backend leverages Spring Boot 3.2 microservices with both relational (MySQL) and document (MongoDB) databases to optimize for each service's unique data model.

**Current State:** The platform is in active development with core marketplace functionality operational—application publishing, user onboarding, profile management, and subscription plan configuration. Next phases include enhanced payment gateway integrations, advanced analytics dashboards for developers, and intelligent application recommendation algorithms to drive user engagement.

---

## 🏗️ Architecture

### Service Topology & Communication Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     FRONTEND (Angular 19 SPA)                           │
│                    http://localhost:4200                                │
│  ┌─────────────────────────────────────────────────────────────┐        │
│  │ • Dashboard & Discovery                                     │        │
│  │ • Application Management (Create/Update/View)               │        │
│  │ • Developer Onboarding & Profile                           │        │
│  │ • Subscription & Cart Management                           │        │
│  │ • Role-Based Access Control (RBAC)                         │        │
│  └─────────────────────────────────────────────────────────────┘        │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │ HTTPS/REST
                                   ▼
        ┌──────────────────────────────────────────────┐
        │   API Gateway (Spring Cloud Gateway MVC)     │
        │           Port: 9000                         │
        │  • Request Routing & Aggregation             │
        │  • OAuth 2.0 Token Validation                │
        │  • Circuit Breaker (Resilience4j)            │
        │  • Load Balancing                            │
        │  • Swagger UI (Aggregated)                   │
        └──────────────────────┬───────────────────────┘
                               │
        ┌──────────────────────┼───────────────────────┐
        │                      │                       │
        ▼                      ▼                       ▼
    ┌──────────┐          ┌──────────┐          ┌──────────────┐
    │ IDENTITY │          │   APP    │          │  DEVELOPER   │
    │ SERVICE  │          │ SERVICE  │          │   SERVICE    │
    │ 8085     │          │ 8080     │          │     8081     │
    │          │          │          │          │              │
    │ Keycloak │          │ MongoDB  │          │  MySQL 3307  │
    │ Admin    │          │ 27017    │          │              │
    └──────────┘          └──────────┘          └──────────────┘
        │                      │                      │
        │                      │                      ▼
        │                      │              ┌──────────────┐
        │                      │              │ USER SERVICE │
        │                      │              │    8082      │
        │                      │              │ MySQL 3308   │
        │                      │              └──────────────┘
        │                      │                      │
        │                      ▼                      ▼
        │              ┌────────────────┐      ┌──────────────┐
        │              │ CART SERVICE   │      │ ORDER        │
        │              │     8083       │      │ SERVICE      │
        │              │  MySQL 3309    │      │    8084      │
        │              └────────────────┘      │ MySQL 3310   │
        │                      │               └──────────────┘
        │                      │                      │
        ▼                      ▼                      ▼
    ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
    │ SUBSCRIPTION     │ │ PAYMENT SERVICE  │ │ NOTIFICATION     │
    │ SERVICE          │ │      8088        │ │ SERVICE          │
    │    8086          │ │  MySQL 3313      │ │     8087         │
    │ MySQL 3312       │ │  • Payments      │ │  (Kafka Consumer)│
    │                  │ │  • Refunds       │ │  • Email Sender  │
    └──────────────────┘ └──────────────────┘ └──────────────────┘
                               │
        ┌──────────────────────┼──────────────────────────┐
        │                      │                         │
        ▼                      ▼                         ▼
    ┌─────────────┐       ┌──────────┐          ┌──────────────┐
    │   KAFKA     │       │ KEYCLOAK │          │   DATABASE   │
    │   9092      │       │  8181    │          │   STORE      │
    │ • Topics:   │       │ OIDC/    │          │              │
    │ - Payments  │       │ OAuth2   │          │ MySQL Pool   │
    │ - Orders    │       │ Realm:   │          │ (shared by    │
    │ - User      │       │ appverse │          │  6 services)  │
    │   Events    │       └──────────┘          └──────────────┘
    │ - Notifications     Host:
    └─────────────┘       localhost:3307-3313
```

### Service Interactions Matrix

| From → To | Method | Purpose |
|-----------|--------|---------|
| API Gateway → Services | REST/HTTP | Route, auth, aggregate responses |
| App Service → Kafka | Event Publish | Emit app lifecycle events |
| Notification Service → Kafka | Event Subscribe | Consume events, send emails |
| Payment Service → Notification Service | HTTP (OpenFeign) | Notify payment status changes |
| Order Service → Subscription Service | HTTP (OpenFeign) | Fetch subscription details |
| All Services → Keycloak | OIDC/OAuth2 | Token validation, user claims |
| All Services → MySQL | JDBC | CRUD operations on business data |
| App Service → MongoDB | Spring Data Mongo | Store/retrieve app metadata |
---

## 💻 Tech Stack

| **Category** | **Technology** | **Version** | **Purpose** |
|---|---|---|---|
| **Frontend** | Angular | 19.2.18 | SPA framework with standalone components |
| | Angular Material | 19.2.18 | Material Design UI components |
| | PrimeNG | 19.1.3 | Advanced data table & UI widget library |
| | RxJS | 7.8.0 | Reactive programming & async handling |
| | TypeScript | 5.8.3 | Type-safe JavaScript |
| | Bootstrap | 5.3.8 | CSS utilities and layout grid |
| **Backend - Core** | Spring Boot | 3.2.5 | REST API & microservice framework |
| | Java | 21 (GatewayApp), 17 (Notification) | JVM runtime |
| | Maven | 3.x | Build automation & dependency management |
| **Backend - Cloud** | Spring Cloud | 2023.0.x | Microservices patterns & cloud native tools |
| | Spring Cloud Gateway | 4.x | API gateway with routing & load balancing |
| | OpenFeign | 4.x | Declarative HTTP client for service-to-service calls |
| | Resilience4j | 2.x | Circuit breaker, bulkhead, retry patterns |
| **Security** | Keycloak | 26.2.0 | OIDC/OAuth 2.0 identity provider |
| | Spring Security | 6.2.x | Servlet-based security framework |
| | Spring OAuth 2.0 | 6.2.x | Resource server & authorization |
| **Data - Relational** | MySQL | 8.0 | Relational database for transactional services |
| | Spring Data JPA | 3.2.x | ORM abstraction layer |
| | Flyway | 9.x | Database migration tool |
| **Data - Document** | MongoDB | 7.x | NoSQL document database for app catalog |
| | Spring Data MongoDB | 4.x | MongoDB abstraction layer |
| **Async & Events** | Apache Kafka | 3.x | Event streaming & async communication |
| | Spring Kafka | 3.x | Kafka producer/consumer framework |
| **Email & Notifications** | JavaMail | 1.6.x | SMTP email sending |
| | Spring Mail | 6.x | Abstraction for mail operations |
| **Code Quality** | Lombok | 1.18.40+ | Reduce boilerplate (getters, setters, constructors) |
| | MapStruct | 1.5.5 | Type-safe DTO mapping & transformation |
| **API Documentation** | SpringDoc OpenAPI | 2.5.0 | Auto-generated Swagger/OpenAPI documentation |
| | Swagger UI | 4.x | Interactive API browser |
| **DevOps & Deployment** | Docker | Latest | Container orchestration & packaging |
| | Docker Compose | Latest | Local multi-container orchestration |
| | Nginx | Latest | Reverse proxy & load balancer |

---

## 🚀 Microservices Breakdown

### 1. **API Gateway** (Port: 9000)
- **Responsibility:** Central entry point for all client requests; routes to appropriate backend services; aggregates responses; enforces cross-cutting concerns like OAuth 2.0 token validation, CORS, rate limiting.
- **Key Endpoints:** `/api/v1/**` (aggregated routes to all services)
- **Database:** None (stateless)
- **Communication:** Routes HTTP requests to downstream services via Spring Cloud Gateway MVC with Resilience4j circuit breaker protection.
- **Notable Features:** Aggregated Swagger UI at `/swagger-ui.html`; OpenFeign for inter-service calls; Load balancing with Spring Cloud LoadBalancer.

### 2. **App Service** (Port: 8080)
- **Responsibility:** Core application catalog management; handles CRUD for applications, categories, file uploads (thumbnails & screenshots); manages app lifecycle (draft, published, archived).
- **Key Endpoints:** `POST /api/v1/applications`, `GET /api/v1/applications/{id}`, `PUT /api/v1/applications/{id}`, `DELETE /api/v1/applications/{id}`, `GET /api/v1/categories`, `GET /api/v1/applications/{id}/images/{type}/{filename}`
- **Database:** MongoDB (`localhost:27017/app-service`)
- **Communication:** Publishes app lifecycle events to Kafka; integrates with Category Service via HTTP; stores file uploads in local `uploads/` directory.
- **Notable Features:** File upload handling (thumbnail + screenshots); image serving via REST endpoint; document-based schema for flexible app metadata.

### 3. **Developer Service** (Port: 8081)
- **Responsibility:** Developer profiles, onboarding, verification status, developer type (individual vs. organization), personal details, and dashboard analytics.
- **Key Endpoints:** `POST /api/v1/developers`, `GET /api/v1/developers/{id}`, `PUT /api/v1/developers/{id}`, `PATCH /api/v1/developers/{id}/verify`, `GET /api/v1/developers/{id}/dashboard`
- **Database:** MySQL (`localhost:3307/developer_service`)
- **Communication:** Calls User Service to link developer profiles with Keycloak identities; publishes developer events to Kafka.
- **Notable Features:** Flyway migrations for schema versioning; role-based dashboard views; developer verification workflow.

### 4. **User Service** (Port: 8082)
- **Responsibility:** End-user profile management, personal details, contact information, subscription history, preferences.
- **Key Endpoints:** `POST /api/v1/users`, `GET /api/v1/users/{id}`, `PUT /api/v1/users/{id}`, `GET /api/v1/users/{id}/subscriptions`
- **Database:** MySQL (`localhost:3308/user_service`)
- **Communication:** Syncs with Keycloak for user identity; publishes user lifecycle events.
- **Notable Features:** Profile completion workflow; tracks user subscriptions and purchase history; Flyway migrations.

### 5. **Cart Service** (Port: 8083)
- **Responsibility:** Shopping cart management; add/remove items; calculate totals; manage cart state per user session.
- **Key Endpoints:** `POST /api/v1/carts`, `GET /api/v1/carts/{userId}`, `PUT /api/v1/carts/{id}`, `DELETE /api/v1/carts/{id}/items/{appId}`
- **Database:** MySQL (`localhost:3309/cart_service`)
- **Communication:** Fetches application details from App Service; coordinates with Order Service during checkout; integrates with Payment Service.
- **Notable Features:** Session-based cart isolation; item de-duplication; real-time subtotal/tax/total calculations.

### 6. **Order Service** (Port: 8084)
- **Responsibility:** Order lifecycle management; converts carts to orders; tracks order status (pending, confirmed, shipped, delivered, cancelled); order history.
- **Key Endpoints:** `POST /api/v1/orders`, `GET /api/v1/orders/{id}`, `GET /api/v1/orders/user/{userId}`, `PATCH /api/v1/orders/{id}/status`
- **Database:** MySQL (`localhost:3310/order_service`)
- **Communication:** Consumes from Cart Service; coordinates with Payment Service for payment processing; publishes order events to Kafka.
- **Notable Features:** Order state machine; audit trail for status changes; tax/shipping calculations.

### 7. **Subscription Service** (Port: 8086)
- **Responsibility:** Subscription plan creation and management; links subscription plans to applications; tracks active user subscriptions; renewal logic; plan hierarchy (basic, pro, enterprise).
- **Key Endpoints:** `POST /api/v1/subscription-plans`, `GET /api/v1/subscription-plans/{id}`, `PUT /api/v1/subscription-plans/{id}`, `DELETE /api/v1/subscription-plans/{id}`, `POST /api/v1/subscriptions`, `GET /api/v1/subscriptions/user/{userId}`
- **Database:** MySQL (`localhost:3312/subscription_service`)
- **Communication:** Links with App Service for plan-to-app mapping; publishes subscription events (created, renewed, cancelled) to Kafka.
- **Notable Features:** Multiple billing intervals (day, week, month, quarter, year); trial period support; automatic renewal handling; billing interval count customization.

### 8. **Payment Service** (Port: 8088)
- **Responsibility:** Payment processing, transactions, receipts, refunds, payment method storage (securely), PCI compliance.
- **Key Endpoints:** `POST /api/v1/payments`, `GET /api/v1/payments/{id}`, `PUT /api/v1/payments/{id}/refund`, `GET /api/v1/payments/order/{orderId}`, `POST /api/v1/payment-methods`
- **Database:** MySQL (`localhost:3313/payment_service`)
- **Communication:** Consumes Order Service events; publishes payment status events to Kafka (consumed by Notification Service); calls external payment gateway APIs.
- **Notable Features:** Payment method management; transaction audit trail; automatic retry logic; PCI compliance via tokenization.

### 9. **Identity Service** (Port: 8085)
- **Responsibility:** Keycloak admin operations; user role assignment; token management; realm configuration.
- **Key Endpoints:** `POST /api/v1/identity/register`, `PATCH /api/v1/identity/users/{userId}/roles`, `GET /api/v1/identity/token/validate`
- **Database:** Keycloak PostgreSQL (external)
- **Communication:** REST API wrapper around Keycloak Admin REST API; enforces role-based access control (RBAC).
- **Notable Features:** OpenFeign client for Keycloak admin operations; role assignment workflow; realm management.

### 10. **Notification Service** (Port: 8087)
- **Responsibility:** Event-driven notifications; email dispatch; notification templates; retry logic; delivery tracking.
- **Key Endpoints:** `POST /api/v1/notifications`, `GET /api/v1/notifications/{id}`, `GET /api/v1/notifications/user/{userId}`
- **Database:** MySQL (shared pool)
- **Communication:** Kafka consumer for payment, order, subscription, and user events; sends emails via SMTP (JavaMail); Spring Mail abstractions.
- **Notable Features:** Template-based email generation; async processing via Kafka; exponential backoff retry; delivery status tracking.

---

## ✨ Key Features

1. **Dual-Role Authentication & Authorization with OpenID Connect (OIDC)** — Integrated Keycloak as the single source of truth for user identity, enabling secure token-based authentication for both developers and end-users while maintaining granular role-based access control (RBAC) across all microservices through OAuth 2.0 resource server validation.

2. **Flexible Application Monetization Models** — Developers can list applications as free, one-time purchase, subscription-only, or hybrid (purchase + subscription), with dynamic pricing in multiple currencies (USD, EUR, GBP, INR), supporting complex billing scenarios and revenue optimization strategies.

3. **Event-Driven Async Architecture with Apache Kafka** — Decoupled services communicate via event streaming (payment events, order confirmations, subscription renewals, user signups) enabling horizontal scalability, system resilience, and real-time notifications without tight coupling or blocking I/O.

4. **Circuit Breaker & Resilience Patterns** — Implemented Resilience4j circuit breakers at the API Gateway and inter-service call layers to gracefully handle service failures, prevent cascading outages, and automatically recover when downstream services become available again.

5. **Microservices API Aggregation & Intelligent Routing** — Spring Cloud Gateway MVC consolidates 10 backend microservices behind a single API gateway, providing unified authentication, request routing, load balancing, and aggregated OpenAPI/Swagger documentation accessible to frontend clients.

6. **File Upload & Media Management** — Developers upload application thumbnails and screenshots; content is stored in local filesystem with blob streaming over HTTP, supporting multiple file types and image transformations for responsive UI rendering across devices.

7. **Subscription Lifecycle & Recurring Billing** — Full subscription management with configurable billing intervals (daily, weekly, monthly, quarterly, yearly), trial period support, automatic renewal, cancellation workflows, and subscription-to-application linkage for SaaS monetization.

8. **Multi-Database Polyglot Architecture** — Strategic use of MongoDB for flexible app catalog metadata and MySQL for relational transactional data (orders, payments, subscriptions) optimizes query performance, schema flexibility, and operational resilience per use case.

9. **Developer Onboarding & Profile Completion Workflow** — Progressive profiling with mandatory and optional fields, profile completion guards, email verification, developer type classification (individual vs. organization), and dashboard analytics for revenue tracking and app performance metrics.

10. **Shopping Cart & Order Management with State Machines** — Type-safe cart session management with real-time total calculations (subtotal, tax, shipping), seamless checkout flow, order state transitions (pending → confirmed → shipped → delivered), and cancellation workflows with refund coordination.

11. **Flyway Database Migrations & Version Control** — All database schema changes are version-controlled and deployed via Flyway, ensuring reproducible deployments, zero-downtime updates, and audit trails for schema evolution across all MySQL-backed microservices.

12. **Standardized Response & Error Handling** — Unified API response envelope with status codes, error messages, and field-level validation errors; OpenFeign client ensures consistent error translation across service boundaries; centralized exception handling at the API Gateway layer.
- Keycloak is configured at `http://localhost:8181` with realm `appverse`.
- Services validate JWTs via `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
- Clients configured in properties include:
  - `api-gateway`, `app-service`, `developer-service`, `user-service`, `order-service`, `cart-service`, `payment-service`, `subscription-service`, `notification-service`.
  - `appverse-frontend` for the Angular app (`frontend/appverse/src/app/auth/keycloak.ts`).
- Role checks are present in some endpoints (e.g., `hasRole('ADMIN')`, `hasRole('developer')`).

## API surface (by service)
All endpoints below are taken from the controllers in this repo.

### API Gateway (`api-gateway`)
- `GET /fallback` - Simple fallback response for degraded upstreams.
- Swagger UI: `http://localhost:9000/swagger-ui.html`

### App Service (`app-service`)
Base path: `/api/apps`
- `POST /api/apps` - Create application (multipart form; developer role required).
- `PUT /api/apps/{id}` - Update application (multipart form).
- `DELETE /api/apps/{id}` - Delete application.
- `GET /api/apps/{id}` - Get application by id.
- `GET /api/apps` - Get all applications.
- `GET /api/apps/my-apps` - Get apps for current developer.
- `GET /api/apps/test` - Test endpoint.
- `GET /api/apps/test-auth` - Auth test.
- `GET /api/apps/images/{type}/{filename}` - Serve uploaded images.

Categories (`/api/categories`)
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`
- `GET /api/categories/{id}`
- `GET /api/categories`

### Developer Service (`developer-service`)
Base path: `/api/developers`
- `POST /api/developers` - Create developer profile.
- `PUT /api/developers/{id}` - Update developer profile.
- `DELETE /api/developers/{id}` - Delete developer profile.
- `GET /api/developers/me` - Get current developer profile.
- `GET /api/developers/exists/by-keycloak-id/{keycloakUserId}` - Check existence.
- `GET /api/developers/internal/{developerId}/email` - Internal email lookup.

### User Service (`user-service`)
Base path: `/api/v1/users`
- `POST /api/v1/users` - Create user profile.
- `GET /api/v1/users/me` - Get current user profile.
- `PUT /api/v1/users/me` - Update current user profile.
- `DELETE /api/v1/users/me` - Delete current user profile.
- `GET /api/v1/users/exists` - Check if profile exists for current user.

### Cart Service (`cart-service`)
Base path: `/api/v1/carts`
- `GET /api/v1/carts/mine` - Get or create current user cart.
- `POST /api/v1/carts/mine/items` - Add item to cart.
- `PUT /api/v1/carts/mine/items/{applicationId}` - Update quantity.
- `DELETE /api/v1/carts/mine/items/{applicationId}` - Remove item.
- `DELETE /api/v1/carts/mine` - Clear cart.
- `GET /api/v1/carts/user/{userId}` - Admin: get cart by user ID.

### Order Service (`order-service`)
Base path: `/api/v1/orders`
- `POST /api/v1/orders` - Create order.
- `GET /api/v1/orders/{orderId}` - Get order by ID.
- `GET /api/v1/orders/mine` - Get current user orders.
- `POST /api/v1/orders/internal/payment-update` - Internal payment update.
- `POST /api/v1/orders/{orderId}/cancel` - Cancel order.
- `GET /api/v1/orders/admin/{orderId}` - Admin: get order.
- `GET /api/v1/orders/admin/user/{userId}` - Admin: get all orders for user.

### Payment Service (`payment-service`)
Base path: `/api/v1/payments`
- `POST /api/v1/payments/initiate` - Initiate payment.
- `POST /api/v1/payments/webhook/{gateway}` - Gateway webhook placeholder.
- `POST /api/v1/payments/internal/mock-payment-update` - Mock status update (dev/test).
- `GET /api/v1/payments/transactions/{paymentTransactionId}` - Get transaction by id.
- `GET /api/v1/payments/transactions/mine` - Get my transactions.
- `POST /api/v1/payments/methods` - Add stored payment method.
- `GET /api/v1/payments/methods/mine` - List stored payment methods.
- `DELETE /api/v1/payments/methods/{storedPaymentMethodId}` - Remove stored payment method.
- `PUT /api/v1/payments/methods/{storedPaymentMethodId}/default` - Set default method.

### Subscription Service (`subscription-service`)
Base path: `/api/v1/subscription-plans`
- `POST /api/v1/subscription-plans/internal` - Create developer plan.
- `POST /api/v1/subscription-plans/{planId}/activate`
- `POST /api/v1/subscription-plans/{planId}/deactivate`
- `GET /api/v1/subscription-plans/application/{applicationId}`

User subscriptions (`/api/v1/subscriptions`)
- `POST /api/v1/subscriptions/mine` - Subscribe user.
- `GET /api/v1/subscriptions/mine` - List current user subscriptions.
- `POST /api/v1/subscriptions/mine/{subscriptionId}/cancel` - Cancel subscription.

### Identity Service (`identity-service`)
Base path: `/api/identity/users`
- `GET /api/identity/users/me` - Current Keycloak user.
- `GET /api/identity/users/{keycloakUserId}` - User by id.
- `POST /api/identity/users/{keycloakUserId}/roles` - Assign roles.
- `POST /api/identity/users/{keycloakUserId}/disable` - Disable user.

## Eventing (Kafka)
The following topics are defined in code:
- `application-events` - Produced by `app-service`.
- `category-events` - Produced by `app-service`.
- `developer-events` - Produced by `developer-service`.
- `cart-events` - Produced by `cart-service`.
- `order-events` - Produced by `order-service`.
- `payment-events` - Produced by `payment-service`.

Observed consumer:
- `notification-service` listens to `application-events` (group `notification-service-v4`).

Notes:
- Subscription event publishing is present in code but currently commented out in `subscription-service`.

## Data model and migrations
Flyway migrations exist for the MySQL-backed services:
- `developer-service/src/main/resources/db/migration`
- `user-service/src/main/resources/db/migration`
- `cart-service/src/main/resources/db/migration`
- `order-service/src/main/resources/db/migration`
- `payment-service/src/main/resources/db/migration`
- `subscription-service/src/main/resources/db/migration`

MongoDB is used by `app-service` and configured at `spring.data.mongodb.uri`.

## Local setup
1. Start Keycloak on port `8181` and create realm `appverse` with required clients.
2. Start MySQL and create the databases listed above, or update each `application.properties` to match your environment.
3. Start MongoDB on `27017`.
4. Start Kafka on `9092`.
5. Run backend services from their folders.

```powershell
# Example for one service
cd "api-gateway"
.\mvnw spring-boot:run
```

## Frontend
```powershell
cd "frontend\appverse"
pnpm install
pnpm start
```

The Angular app runs at `http://localhost:4200` and authenticates via Keycloak at `http://localhost:8181`.

## API documentation
- Gateway aggregated Swagger UI: `http://localhost:9000/swagger-ui.html`
- Individual services expose Swagger UI at `http://localhost:<service-port>/swagger-ui.html`

## Testing
- Standard Spring tests: `./mvnw test` in a service folder.
- User service testing guide: `user-service/TESTING_GUIDE.md`.

## Configuration notes
- `application.properties` files currently include local credentials and client secrets.
- For shared or production environments, move secrets to environment variables or a secrets manager.
- `app-service` uploads are stored in `app-service/uploads/` and served via `/api/apps/images/...`.

## Common request flow examples
- Developer onboarding:
  - Frontend authenticates with Keycloak.
  - `developer-service` creates profile via `/api/developers`.
- App publishing:
  - Developer uploads app data and media to `/api/apps` (multipart).
  - `app-service` persists in MongoDB and publishes `application-events`.
- Shopping and checkout:
  - User adds items to `/api/v1/carts/mine/items`.
  - User creates order via `/api/v1/orders`.
  - Payment initiated via `/api/v1/payments/initiate` and updates order via internal payment update endpoint.
- Subscriptions:
  - Plans created via `/api/v1/subscription-plans/internal`.
  - User subscribes via `/api/v1/subscriptions/mine`.

## Troubleshooting quick checks
- 401/403 errors: verify Keycloak realm, clients, and user roles.
- Database errors: confirm the correct MySQL port and schema per service.
- Kafka errors: confirm broker on `localhost:9092` and topic creation.
- File uploads: ensure `app-service/uploads/` exists and is writable.

