# Database Migration Guide

This guide explains how to evolve the MySQL schema (`mystery_db`) that powers every microservice. Follow these practices whenever you add, remove, or change tables, columns, indexes, or seed data.

## Principles
- **Plan first**: understand how the change affects each service and the data already in production-like environments.
- **Version every change**: track migrations as numbered SQL files so teammates can apply them in order.
- **Keep migrations idempotent when possible**: guard against re-running the script (e.g., `CREATE TABLE IF NOT EXISTS`).
- **Test locally before sharing**: run migrations against your local MySQL and the Docker Compose MySQL container.

## Folder Structure
Create migration scripts under `config/db/migration` (create the folder if it does not exist) and commit them. Suggested naming convention:

```
config/db/migration/V{timestamp}__{short_description}.sql
```

Example: `V20260106_1200__add_account_limits.sql`

## Workflow
1. **Design the schema change**
   - Sketch the new table/column definition.
   - Identify data backfills or cleanup queries needed.
2. **Write the migration script**
   - Use plain SQL compatible with MySQL 8.
   - Include `BEGIN;` / `COMMIT;` if the change spans multiple statements that must succeed together.
3. **Apply locally (host MySQL)**
   ```bash
   mysql --protocol=TCP -h 127.0.0.1 -P 3306 -ubaas_user -pbaas_password mystery_db < config/db/migration/V20260106_1200__add_account_limits.sql
   ```
4. **Apply inside Docker Compose MySQL** (if it is running)
   ```bash
   docker compose exec mysql mysql -ubaas_user -pbaas_password mystery_db < /app/config/db/migration/V20260106_1200__add_account_limits.sql
   ```
5. **Verify**
   - Run `DESCRIBE table_name;` or appropriate SELECTs.
   - Ensure services start without Hibernate errors.
6. **Commit and document**
   - Commit the SQL file with a clear message.
   - Reference the migration in pull request notes and in relevant service READMEs if necessary.

## Rolling Back
- Prefer forward-only migrations. If a rollback is essential, create a new `V...__rollback_*.sql` script that undoes the change.
- Never edit an already-applied migration; add a new one to correct mistakes.

## Tips
- When adding nullable columns to large tables, backfill in batches to avoid lock contention.
- Use `ALTER TABLE ... ADD COLUMN ... DEFAULT ...` carefully—MySQL rewrites the table.
- For destructive operations, take a dump first: `mysqldump mystery_db > backup.sql`.

Following this process keeps schema changes reproducible across laptops, CI pipelines, and production environments.
