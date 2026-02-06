# AppVerse

AppVerse is a microservices-based platform with a Spring Boot backend and an Angular frontend. It includes an API gateway, identity integration with Keycloak, and domain services for apps, developers, users, carts, orders, payments, subscriptions, and notifications.

## Architecture at a glance
```
[ Angular Frontend ]
        |
        v
[ API Gateway :9000 ]
        |
        +--> app-service :8080 (MongoDB)
        +--> developer-service :8081 (MySQL)
        +--> user-service :8082 (MySQL)
        +--> cart-service :8083 (MySQL)
        +--> order-service :8084 (MySQL)
        +--> identity-service :8085 (Keycloak admin)
        +--> subscription-service :8086 (MySQL)
        +--> notification-service :8087 (Kafka + SMTP)
        +--> payment-service :8088 (MySQL)

Keycloak (OIDC) :8181
Kafka :9092
```

## Repository layout
- `api-gateway` - API gateway with aggregated Swagger UI and circuit breakers.
- `app-service` - Application catalog, categories, and uploads (MongoDB).
- `cart-service` - Cart management (MySQL).
- `developer-service` - Developer profiles and onboarding (MySQL).
- `identity-service` - Keycloak admin integration and identity APIs.
- `notification-service` - Event-driven email notifications.
- `order-service` - Orders and lifecycle (MySQL).
- `payment-service` - Payments and stored methods (MySQL).
- `subscription-service` - Subscription plans and user subscriptions (MySQL).
- `user-service` - User profiles linked to Keycloak (MySQL).
- `frontend/appverse` - Angular web client.
- `uploads` - Local upload storage (used by `app-service`).

## Services, ports, and data stores
| Service | Port | Data store | Key notes |
| --- | --- | --- | --- |
| `api-gateway` | 9000 | None | CORS for `http://localhost:4200` and `http://localhost:3000`. Aggregated Swagger UI. |
| `app-service` | 8080 | MongoDB `localhost:27017/app-service` | File uploads stored in `app-service/uploads/`. |
| `developer-service` | 8081 | MySQL `localhost:3307/developer_service` | Uses Flyway migrations. |
| `user-service` | 8082 | MySQL `localhost:3308/user_service` | Uses Flyway migrations. |
| `cart-service` | 8083 | MySQL `localhost:3309/cart_service` | Uses Flyway migrations. |
| `order-service` | 8084 | MySQL `localhost:3310/order_service` | Uses Flyway migrations. |
| `identity-service` | 8085 | None | Keycloak admin client integration. |
| `subscription-service` | 8086 | MySQL `localhost:3312/subscription_service` | Uses Flyway migrations. |
| `notification-service` | 8087 | None | Kafka consumer and SMTP (Mailtrap configured). |
| `payment-service` | 8088 | MySQL `localhost:3311/payment_service` | Uses Flyway migrations. |

## Tech stack
- Java 21 for most services, Java 17 for `identity-service` and `notification-service` (see each `pom.xml`).
- Spring Boot, Spring Security (OAuth2 Resource Server), Spring Cloud Gateway, OpenFeign, Resilience4j.
- Spring Data JPA, Flyway, MySQL, MongoDB, Kafka.
- Angular 19 (CLI 19.2.18), PrimeNG, Keycloak JS.

## Authentication and authorization
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

