# QBit CMS

[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/java-21+-blue.svg)](https://adoptium.net/)

> QQQ QBit: a headless CMS (sites, pages, menus, blog, media, projects, photos and publishing) as standard QQQ tables.

**Status:** design approved: [QRun-IO/qqq#1083](https://github.com/QRun-IO/qqq/pull/1083) (see [docs/DESIGN.md](docs/DESIGN.md)); implementation in progress. Nothing is published yet.

## Requirements

- Java 21.
- QQQ 4.1 (built against `4.1.0-RC.4`) with an RDBMS backend (PostgreSQL in production, H2 for tests).
- QQQ Next UI 1.0 (`1.0.0-RC.14` or later). Material is not supported.

## Install

Not yet published. Once released, add `com.kingsrook.qbits:qbit-cms` and produce the qbit into your `QInstance`:

```java
new CmsQBitProducer()
   .withQBitConfig(new CmsQBitConfig().withBackendName("rdbms"))
   .produce(qInstance);
```

## Naming

Tables are `cms_<name>` (lowercase snake_case, backend name equal to the table name). Permissions are `cms.<table>.<action>` and `cms.<action>`.

## Adopting in another app

Follow the QQQ [adoption checklist](https://github.com/QRun-IO/qqq/blob/develop/docs/migration/adoption-checklist.adoc) once the first release is published.

## Build

```bash
./mvnw verify
```

Always use the Maven wrapper (Maven 3.9.11); Maven 3.10 cannot read the parent POM.

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
