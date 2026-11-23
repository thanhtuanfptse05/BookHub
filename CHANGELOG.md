# Changelog

## 1.0.0 (2026-11-23)


### Features

* **admin-catalog:** implement Admin Categories and Books CRUD, soft-deactivate, and AC-01..07 tests ([89e788f](https://github.com/thanhtuanfptse05/BookHub/commit/89e788f53ebadc67a240d129756085966def7775))
* **admin-reports:** implement Admin Dashboard, Users lock/unlock, and SQL aggregate Reports ([343674e](https://github.com/thanhtuanfptse05/BookHub/commit/343674ea65f0fe55a247444eb8fc14067df0fb95))
* **auth:** implement register, login, logout, reset-password, profile, and AC-01..10 tests ([cb6007c](https://github.com/thanhtuanfptse05/BookHub/commit/cb6007cc7c1aa9e74e68a54a5ae47748b1b51ba6))
* **catalog:** implement public Home, Books catalogue, Book detail, and AC-01..08 tests ([5a0ee73](https://github.com/thanhtuanfptse05/BookHub/commit/5a0ee7328eecd28047b572396a438ede64d57236))
* **config:** add persistence.xml, AppConfig, JpaUtil, and lifecycle listener ([aa3c44b](https://github.com/thanhtuanfptse05/BookHub/commit/aa3c44baa9e862aebef324a741d74bd217164557))
* **database:** add SQL Server schema, seed data, and audit queries ([8b4d3ee](https://github.com/thanhtuanfptse05/BookHub/commit/8b4d3eeefeb86e5ad9e5d0fed3db1a5f6d414595))
* **entity:** map all 12 SQL Server tables and 6 domain enums with JPA ([59a1710](https://github.com/thanhtuanfptse05/BookHub/commit/59a171089a48e4ea491b3cdffdc189c9ecd4bc25))
* **orders:** implement Cart, atomic COD Checkout, Order lifecycle, and AC-01..09 concurrency tests ([5102566](https://github.com/thanhtuanfptse05/BookHub/commit/5102566493b81acbfe74506ad246a0a04b2214f8))
* **repository:** add BaseRepository, smoke repositories, and AC-01..08 tests ([c231e7d](https://github.com/thanhtuanfptse05/BookHub/commit/c231e7d490279ea0e2324112523715adddcc3d91))
* **reviews-qa:** implement verified Reviews, Admin-only Q&A, moderation, and AC-01..08 tests ([50eebe1](https://github.com/thanhtuanfptse05/BookHub/commit/50eebe14df058d514d0c7a9595d68a3beb243e06))
* **shell:** add MVC security filters, web.xml, Figma tokens, shared JSP shell, and AC-01..07 tests ([6514c7d](https://github.com/thanhtuanfptse05/BookHub/commit/6514c7dabdebb862aee035c41f0023df6f6ae1d6))


### Bug Fixes

* **config:** discover bookhub-local.properties from ServletContext root during NetBeans in-place deploy ([607950c](https://github.com/thanhtuanfptse05/BookHub/commit/607950c974cd32d90da12bc63a1a62624f281b06))
* **ui:** fix SVG UTF-8 mojibake on Windows Tomcat and add 26 real book covers ([d7ca02c](https://github.com/thanhtuanfptse05/BookHub/commit/d7ca02c10498930ad2251a4e11be149d3ebe565a))
* **web:** fix JSP EL reserved empty keyword and JPQL ORDER BY spacing ([1b1fae2](https://github.com/thanhtuanfptse05/BookHub/commit/1b1fae21fa98a720c06d9b81b0f04d735b4936f9))
