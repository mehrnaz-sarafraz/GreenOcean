# GreenOcean Architecture

## Architecture style

GreenOcean is currently implemented as a modular monolith.

## Frontend

React Native + Expo + TypeScript.

The frontend is organized around screens, reusable components, feature modules, localization, API infrastructure, storage, and theme tokens.

## Backend

Java 21 + Spring Boot.

Backend packages are organized by domain rather than by global technical layer.

Current domains include:

- auth
- catalog
- community
- messaging
- moderation
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

Deployment architecture has not yet been finalized.

Do not introduce microservices, message brokers, distributed caches, or other infrastructure until actual product requirements justify them.