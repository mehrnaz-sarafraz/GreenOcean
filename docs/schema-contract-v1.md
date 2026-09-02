# GreenOcean Database Schema Contract — V1

## Purpose

This document records the durable database rules and the original V1 domain contract for GreenOcean.

It is documentation, not an executable schema definition.

The authoritative source of truth for the current database schema is:

`backend/src/main/resources/db/migration/`

## Current migration state

The GreenOcean database currently evolves through Flyway migrations V1–V13.

Existing migrations that have already been applied must not be edited.

Every future schema change must be introduced through a new migration, beginning with V14.

## Original V1 domain contract

The original database contract defined the following core domains.

### Authentication

* users
* profiles
* user_sessions
* roles
* user_roles

### Social

* follows
* blocks
* bookmarks

### Content

* posts
* comments
* media
* tags
* post_tags

### Community

* communities
* community_members

### Professionals

* professional_profiles
* professional_verifications

### Engagement

* post_likes
* comment_likes
* notifications

### Safety

* reports
* audit_logs

## Global database rules

* Primary identifiers should use UUIDs.
* Application timestamps use timezone-aware PostgreSQL timestamps.
* Email and username uniqueness must be case-insensitive.
* Relationship tables should use composite keys where appropriate.
* Foreign-key deletion behavior must be explicit.
* Database constraints should enforce important domain invariants.
* Passwords must only be stored as secure hashes.
* Refresh tokens must only be stored as hashes.
* JWT access tokens must not be persisted in the database.
* Likes remain domain-specific rather than being stored in a generic polymorphic table.
* Flyway is the only supported mechanism for schema evolution.

## Schema evolution

The original contract has evolved through later migrations to support the current application.

When this document and the executable migrations disagree, the migrations represent the current database implementation.

Architectural intent should then be reviewed and this document updated accordingly.

## Migration policy

Never:

* modify a migration that has already been applied;
* manually modify the production schema without a migration;
* delete rows from `flyway_schema_history`;
* change migration checksums manually to hide an inconsistency.

Instead, introduce a new versioned migration.

Example:

`V14__add_example_feature.sql`
