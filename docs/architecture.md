# GreenOcean Architecture

## Architecture style

GreenOcean is currently implemented as a modular monolith.

## Frontend

React Native + Expo + TypeScript.

Expo Router files are thin route adapters. Large implementations live in feature modules. A small query client provides request de-duplication, stale-time caching, invalidation, and incremental collection loading without coupling the application to a specific server-state library.

## Backend

Java 21 + Spring Boot.

Backend packages are organized by domain rather than by global technical layer.

Current domains include:

- auth
- catalog
- community
- messaging
- media
- moderation
- professional
- notification
- post
- preference
- profile
- search
- social
- system

## Database

PostgreSQL is the primary relational database.

Flyway migrations are the only supported mechanism for schema changes.

## Authentication

GreenOcean uses short-lived JWT access tokens and managed rotating refresh-token sessions.

Passwords and refresh tokens must never be stored in plaintext.

## Deployment

The production baseline is a modular monolith, PostgreSQL 18, durable media storage and a statically exported web client served by Nginx. `compose.production.yml` is an operable reference topology, not a public deployment. Internet ingress/TLS, secret management, encrypted managed storage and the external backup destination are environment-owned responsibilities.

Media metadata and ownership live in PostgreSQL; bytes live behind the `MediaStorage` abstraction. The current adapter uses a mounted durable filesystem. Pending uploads expire automatically and attached-media access follows post visibility and block rules.

Do not introduce microservices, message brokers, or distributed caches until measured product requirements justify them.
