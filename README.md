# QBit CMS

[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/java-21+-blue.svg)](https://adoptium.net/)

> QQQ QBit: a headless CMS (sites, pages, menus, blog, media, projects, photos and publishing) as standard QQQ tables.

**Status:** design approved in [QRun-IO/qqq#1083](https://github.com/QRun-IO/qqq/pull/1083) (see [docs/DESIGN.md](docs/DESIGN.md)). The CORE feature is implemented; MEDIA, BLOG, PROJECTS, PHOTOS, PUBLISHING and STREAM are next. Nothing is published yet.

The CMS is extracted from the kof22 website, which runs it in production. Its data lives in ordinary QQQ tables in your own database, secured by QQQ permissions. It brings an admin app with one home per area, and a schema you run with your own Liquibase.

## Requirements

- Java 21.
- QQQ 4.1 (built against `4.1.0-RC.4`) with an RDBMS backend: PostgreSQL 14+ in production, H2 for tests.
- QQQ Next UI 1.0 (`1.0.0-RC.14` or later). Material is not supported.
- Liquibase in your migration pipeline (or the reference DDL in `docs/ddl/postgres.sql`).

## Install

1. **Add the dependency** `com.kingsrook.qbits:qbit-cms` (once released).
2. **Run the schema.** Put the qbit jar on your Liquibase classpath and include its changelog. Set its properties *before* the include (the first definition wins):

   ```yaml
   databaseChangeLog:
     - property: { name: cms.tablePrefix, value: cms_ }      # must equal CmsQBitConfig.tableNamePrefix
     - property: { name: cms.features, value: CORE }          # the features you install
     - include:  { file: qbit-cms/db/changelog/qbit-cms.changelog.yaml }
   ```

   The changelog never touches a table it did not create. If a table with one of its names already exists, it halts with a message telling you to choose another prefix. It seeds only generic reference data (redirect, page and section types); set `cms.seedReferenceData` to `false` to skip that.
3. **Produce the qbit** after your backends (and the parent app, if any) are on the instance:

   ```java
   new CmsQBitProducer()
      .withQBitConfig(new CmsQBitConfig()
         .withBackendName("rdbms")
         .withRole("cms-admins", CmsRole.ADMIN)
         .withRole("cms-editors", CmsRole.EDITOR))
      .produce(qInstance);
   ```

4. **Grant permissions** from your authentication customizer: `CmsSecurity.applySessionPermissions(session, userGroups, config)`.

At startup the producer checks that the changelog ran for every enabled feature and that the tables exist. `startupMode` `FAIL_FAST` (the default) refuses to start and lists what is missing. `DEGRADED` starts and shows the problem on the CMS home. `SKIP` does not check (unit tests without a database).

## Configuration reference (`CmsQBitConfig`)

| Setting | Default | Meaning |
|---|---|---|
| `backendName` | required | The RDBMS backend for every CMS table |
| `tableNamePrefix` | `cms_` | Table name prefix (QQQ and SQL names are equal); must equal the changelog's `cms.tablePrefix` |
| `namePrefix` | `cms` | Prefix of app and widget names (`cmsPages`, `cmsPagesHome`) |
| `permissionBaseName` | `cms` | Base of qbit-wide permissions (`cms.admin`, `cms.publish`) |
| `features` | every available feature | `CmsFeature` set; `CORE` is required, and a feature this version does not implement yet is rejected |
| `appName`, `appLabel`, `parentAppName` | `cms`, `CMS`, none | The top app and where it nests in your app tree |
| `openReads` | `false` | When true, reading CMS tables needs no permission (writes always do) |
| `startupMode` | `FAIL_FAST` | `FAIL_FAST`, `DEGRADED` or `SKIP` (see above) |
| `roleMapping` | empty | Identity-provider group name to `CmsRole` |

A namespace (`withNamespace("siteTwo")`) fills in all three prefixes for a second install in the same instance (`site_two_page`, `siteTwoPages`, `site_two.admin`).

## Features

| Feature | Tables | Status |
|---|---|---|
| `CORE` | site, domain, redirect, redirect type, page, page type, page section, page section type, snippet, menu, menu item | available |
| `MEDIA` | media, media type, storage SPI (`qbit-object-storage` for S3) | next |
| `BLOG` | blog, blog type, post, tags, authors, post media, versions | next |
| `PROJECTS`, `PHOTOS`, `PUBLISHING`, `STREAM`, `DATA_HEALTH_RULES` | see the design | planned |

## The admin

The qbit adds one app (`CMS`) with a home, and one sub-app per area, each with its own home:

- **Sites:** sites (domains are a list on each site) and redirects; Settings has redirect types.
- **Pages:** pages (sections and child pages are lists on each page) and snippets; Settings has page types and page section types.
- **Navigation:** menus (items are a list on each menu).

The nav shows only main tables and reference data; child tables are edited on their parents' records. Each home shows only real data read as the current user: counts, drafts, inactive rows and recently changed records, readable at any width in light and dark mode. The qbit never sets the instance's home app.

## Permissions

Every CMS table has QQQ permission rules. The table permissions are `<table>.read`, `.insert`, `.edit` and `.delete` (for example `cms_page.edit`). The qbit-wide permissions are `cms.admin` and `cms.publish`.

| Role | Grants |
|---|---|
| `ADMIN` | everything, including reference data, `cms.admin` and `cms.publish` |
| `EDITOR` | read everything; write content tables |
| `PUBLISHER` | `EDITOR` plus `cms.publish` |
| `DRAFTER` | read everything; insert and edit content (no deletes, no publishing) |
| `VIEWER` | read everything |

The qbit is built for QQQ core permission enforcement (`qInstance.setEnforcePermissionsInCoreActions(true)`, the default from QQQ 4.2).

## Operational tables

All CMS data is in normal, visible QQQ tables. Nothing is hidden and there is no side store. The schema is in `docs/ddl/postgres.sql` (generated from the changelog).

## Upgrading

Read the CHANGELOG's "Schema changes" before deploying. Run your migrations before the new app version. Released changesets never change; new versions only append changesets.

## Adopting in another app

Follow the QQQ [adoption checklist](https://github.com/QRun-IO/qqq/blob/develop/docs/migration/adoption-checklist.adoc) once the first release is published.

## Build

```bash
./mvnw verify
```

Always use the Maven wrapper (Maven 3.9.11); Maven 3.10 cannot read the parent POM. The PostgreSQL tests use Testcontainers and are skipped when Docker is not available.

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
