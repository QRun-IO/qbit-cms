# Changelog

All notable changes to this project are documented here. The format follows Keep a Changelog; versions follow semantic versioning.

## [Unreleased]

### Added
- Repository scaffold: build, CI and repository standard files.
- `CmsQBitProducer` and `CmsQBitConfig`: table prefix `cms_`, name prefix `cms`, permission base `cms`, namespaces for a second install, validation, and a collision check against names already on the instance.
- `CmsFeature` flags with dependencies. `CORE` is implemented; the other features are rejected until they ship.
- CORE tables as standard QQQ tables: site, domain, redirect, redirect type, page, page type, page section, page section type, snippet, menu and menu item. Each comes with a possible-value source, child lists on parent records, associations (`page.sections`, `menu.items`) and field audit when the host has QQQ audit tables.
- The CMS app with one home per area (Sites, Pages, Navigation). The homes show only real data read as the user and are readable at any width.
- Permissions: per-table rules, `CmsRole` bundles, `CmsSecurity.applySessionPermissions`, and `openReads`.
- Startup schema check (`startupMode` `FAIL_FAST`, `DEGRADED`, `SKIP`).

### Schema changes
- `1.0.0` baseline: a prefix-parameterized Liquibase changelog (`qbit-cms/db/changelog/qbit-cms.changelog.yaml`, logical path `qbit-cms/changelog`). It creates the CORE tables with the kof22 column types, adds their foreign keys and indexes, and seeds generic reference data. Feature sections are gated by `cms.features`, and a name collision halts the install. Reference DDL is in `docs/ddl/postgres.sql`.
