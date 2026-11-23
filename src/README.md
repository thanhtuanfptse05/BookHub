# src/

Cấu trúc mã nguồn **BookHub** (Layered MVC + JPA/Hibernate, Java 17 / Jakarta EE 10):
- `src/java/com/bookhub/` — `config`, `entity`, `dto`, `repository`, `service`, `controller/{site,customer,admin}`, `filter`, `exception`, `util`.
- `src/conf/META-INF/persistence.xml` — cấu hình persistence unit `BookHubPU` (`RESOURCE_LOCAL`, Hibernate 6.6, SQL Server).

Quy tắc phụ thuộc: `Filter → Controller (Servlet) → Service → Repository (JPA) → Entity/DB`. Xem `CLAUDE.md §3` và `docs/architecture.md`.
