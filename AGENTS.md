# AGENTS.md

This file provides guidance to Codex when working with code in this repository.

## Project Overview

**QBit CMS**: QQQ QBit: a headless CMS (sites, pages, menus, blog, media, projects, photos and publishing) as standard QQQ tables, extracted from the kof22 website. Design: [QRun-IO/qqq#1083](https://github.com/QRun-IO/qqq/pull/1083) (decisions D1 to D17, all accepted). Implementation in progress.

## Build Commands

```bash
./mvnw compile          # Compile (always use the wrapper: Maven 3.10 cannot read the parent POM)
./mvnw test             # Unit tests
./mvnw verify           # Unit tests + JaCoCo gate (70% instructions, 90% classes)
```

## Architecture

**Entry point:** `CmsQBitProducer implements QBitMetaDataProducer<CmsQBitConfig>` in `com.kingsrook.qbits.cms`. The producer builds every table, possible-value source, app and widget itself (no package scanning); `CmsFeature` flags switch feature areas on and off. The schema is a prefix-parameterized Liquibase changelog the host includes and runs (`src/main/resources/qbit-cms/db/changelog/`); the qbit never runs DDL.

Packages:

- `CmsQBitConfig`, `CmsFeature`, `CmsTables` (logical names and table definitions), `CmsRuntime`, `CmsSchemaCheck` (startup check), `CmsStartupMode`.
- `core/model`: CORE entities. Their `TABLE_NAME` and possible-value source names are logical; the builder adds the prefix.
- `metadata`: `CmsTableBuilder` (tables, possible values, joins, child lists, associations, permission rules), `CmsAppBuilder` and `CmsAreas` (apps and homes).
- `home`: `CmsHomeRenderer` and `CmsHtml` (one renderer for every home; reads run with `PermissionEnforcement.ENFORCE`).
- `security`: `CmsRole`, `CmsSecurity`, `CmsOpenReadsPermissionChecker`.
- Schema: `src/main/resources/qbit-cms/db/changelog/qbit-cms.changelog.yaml`. Its changesets are append-only once released, and `docs/ddl/postgres.sql` is generated from it.

## Naming (owner rule)

- Tables are lowercase snake_case prefixed with the full qbit name: `cms_<name>`. The QQQ table name and the backend (SQL) name are identical; possible-value sources take the table name.
- Permissions use the full qbit name: `cms.<table>.<action>` and `cms.<action>` (for example `cms.cms_page.edit`, `cms.publish`).
- Qbit data lives in standard, visible QQQ tables, secured by permissions and record security locks, never hidden.
- Processes run as table or record actions, never in the nav.

## Code Style

- 3-space indentation, opening braces on the next line, `return (x);`.
- Javadoc flower-box comments (`/*****` with `**` lines) on every class and method.
- Fluent setters (`withX()`).
- Wrapper types (`Integer`, `Boolean`) in config and entities.

## Dependencies

- Java 21.
- Parent `com.kingsrook:qbit-build-parent:2.0.0` (qqq 4.0.0). The default-active `qqq-snapshot` profile imports `qqq-bom-pom:4.1.0-RC.4` ahead of it, as in qbit-quick-search (ADR-0007). Remove that activation when the parent moves to 4.1.
- Tests: JUnit 5, AssertJ.

## Release

- CircleCI qqq-orb 0.6.8.
- `develop` publishes SNAPSHOTs.
- A `release/*` branch publishes RCs (the orb bumps the version).
- A `v*` tag publishes the release.
