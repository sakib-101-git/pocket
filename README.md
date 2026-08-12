# Pocket

A personal finance backend that retrieves bank transactions automatically to track money movement.

Every line written and understood deliberately not AI-generated. Frontend: [pocket-app](https://github.com/sakib-101-git/pocket-app) (Expo, React Native + TypeScript).

## What it does

A user registers, links a bank account through Plaid, and their transactions sync automatically in the background. This is processed asynchronously through a message queue, with retries and idempotent writes so a duplicated or redelivered sync job never corrupts data.

## Tech stack

**Core:** Java 21 · Spring Boot 4 · Spring Security · Spring Data JPA · Hibernate
**Data:** PostgreSQL · Flyway · Testcontainers
**Async:** RabbitMQ · Spring AMQP 
**Integration:** Plaid API 
**Auth:** JWT (access token) · BCrypt password hashing
**Docs:** springdoc-openapi (Swagger UI)
**Infra:** Docker · Maven
**Deployed on:** Render (API) · Supabase (Postgres) · CloudAMQP (RabbitMQ)
