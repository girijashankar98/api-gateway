# api-gateway

Spring Cloud Gateway service that sits in front of backend APIs. It authenticates callers and forwards requests to the backend with a JWT token.

## How it works
1. Caller sends `key` and `secret` headers. The tenant is the first part of the request host name (e.g. `acme.example.com` -> `acme`).
2. Gateway checks the secret against the tenant config in MongoDB (`tenant_api_config`).
   - headers missing / invalid -> `400`, wrong credentials -> `401`. The request is not forwarded.
3. On success the gateway gets a Keycloak client-credentials token for the tenant realm (cached in Redis for 5 minutes), sets `Authorization: Bearer <token>`, removes `key`/`secret`, strips the `/apigateway` base path and routes to the backend.

Routes: `/apigateway/backend/**` -> `BACKEND-SERVICE` (default `http://localhost:8002`). Swagger UI aggregates the backend API docs.

## Run
Needs Java 17, MongoDB, Redis and the backend service.

```
export KEYCLOAK_CLIENT_SECRET=<secret>
export REDIS_PASSWORD=<password>   # optional
mvn spring-boot:run                # port 7001
```

Secrets are read from environment variables, never commit them.
