# Database Setup Guide

This project expects a local MySQL instance listening on port `3306` with a schema and user that all microservices share. Follow these steps to provision the required database and credentials.

## 1. Ensure MySQL Is Running

Use Homebrew services (or your preferred method) to make sure MySQL is online:

```bash
brew services start mysql
# or start the MySQL app / launchd job that ships with your installation
```

Confirm the server is listening on port 3306:

```bash
/usr/sbin/lsof -i TCP:3306
```

You should see a `mysqld` process bound to `*:mysql`.

## 2. Connect as a Privileged User

Run the MySQL CLI as `root` (or any admin account) over TCP. The command below targets localhost on port 3306; add `-p` if your root account requires a password.

```bash
mysql --protocol=TCP -h 127.0.0.1 -P 3306 -uroot
```

## 3. Create the Shared Schema and User

Execute the following SQL statements from the MySQL prompt (or pass them via the `-e` flag):

```sql
CREATE DATABASE IF NOT EXISTS baas_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'baas_user'@'%'
  IDENTIFIED BY 'baas_password';

GRANT ALL PRIVILEGES ON baas_db.* TO 'baas_user'@'%';
FLUSH PRIVILEGES;
```

Notes:
- The username/password pair **must** match the Spring `application.yml` values (`baas_user` / `baas_password`).
- Granting on `'%'` lets both host processes and Docker containers connect. Restrict to `'localhost'` if you prefer local-only access.

## 4. Verify Connectivity

Use the application credentials to confirm access:

```bash
mysql --protocol=TCP -h 127.0.0.1 -P 3306 -ubaas_user -pbaas_password -e "SHOW DATABASES;"
```

You should see `baas_db` listed alongside the default schemas.

## 5. Troubleshooting

- **Port already in use**: if `docker-compose` fails to start MySQL because port `3306` is occupied, stop your host MySQL (e.g., `brew services stop mysql`) before launching the stack, or edit `docker-compose.yml` to expose MySQL on a different port.
- **Authentication plugin errors**: Homebrew’s MySQL uses `caching_sha2_password` by default. The provided commands rely on the default plugin; avoid forcing `mysql_native_password` unless the server loads that plugin.
- **Firewall / sandbox restrictions**: when running commands from the Codex CLI, you might need elevated permissions for network access. Re-run the command with escalation if you see `Operation not permitted` errors.

With the schema and user in place, all Spring Boot services can connect to `jdbc:mysql://localhost:3306/baas_db` using the shared credentials, and Flyway/JPA will handle table creation automatically on startup.
