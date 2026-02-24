---
name: farmiq-dev
description: Expert assistant for the FarmIQ JavaFX desktop application. Provides guidance on project structure, build commands, coding conventions, database setup, and development workflows specific to this codebase.
---

# FarmIQ Developer Skill

A comprehensive skill for developing and maintaining the FarmIQ farm management desktop application.

## When to Use This Skill

- Setting up the FarmIQ development environment from scratch
- Understanding the project architecture and package layout
- Running, building, or debugging the application
- Adding new features following existing patterns (models, DAOs, services, controllers)
- Troubleshooting database connectivity or configuration issues
- Working with JavaFX FXML views and CSS styling

## Project Overview

FarmIQ is a **JavaFX 21 + Java 17 + MySQL** desktop application built with Maven. It is a back-office farm management system targeting the Tunisian market (locale `fr_TN`, currency `TND`).

## Build & Run Commands

```bash
# Compile the project
mvn clean compile

# Run the application
mvn javafx:run

# Package as a fat JAR
mvn clean package -DskipTests

# Windows convenience scripts
run.bat        # Run on Windows
run-app.ps1   # PowerShell launcher

# Linux/macOS convenience script
./run.sh
```

## Project Structure

```
farmIQ/
├── pom.xml                          # Maven build (Java 17, JavaFX 21)
├── src/main/
│   ├── java/com/farmiq/
│   │   ├── Main.java                # Application entry point
│   │   ├── controllers/             # JavaFX FXML controllers
│   │   ├── dao/                     # Data Access Objects (JDBC)
│   │   ├── exceptions/              # Custom exception classes
│   │   ├── models/                  # Domain model POJOs
│   │   │   └── enums/               # Enum types (e.g. NotificationType)
│   │   ├── services/                # Business logic layer
│   │   └── utils/                   # Utilities (DatabaseConnection, etc.)
│   └── resources/
│       ├── config.properties        # App configuration (DB, mail, weather)
│       ├── log4j2.xml               # Logging configuration
│       └── views/
│           ├── css/                 # Stylesheets (admin.css)
│           └── fxml/                # FXML view files
└── sql/                             # Database schema/seed scripts
```

## Configuration

Configuration lives in `src/main/resources/config.properties`. All database and mail settings support environment variable overrides:

| Property | Env var override | Default |
|---|---|---|
| `db.host` | `DB_HOST` | `localhost` |
| `db.user` | `DB_USER` | `root` |
| `db.password` | `DB_PASSWORD` | _(empty)_ |
| `mail.host` | `MAIL_HOST` | `smtp.gmail.com` |

To configure without editing the file, set environment variables before launching:

```bash
export DB_HOST=myserver DB_USER=farmuser DB_PASSWORD=secret
mvn javafx:run
```

## Database Setup

1. Install **MySQL 8+** and ensure it is running on port `3306`.
2. Create the database:
   ```sql
   CREATE DATABASE farmiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Run the SQL scripts found in the `sql/` directory:
   ```bash
   mysql -u root -p farmiq < sql/schema.sql
   mysql -u root -p farmiq < sql/seed.sql   # if present
   ```
4. Update `config.properties` (or set env vars) with your credentials.

> **Note**: The codebase uses a single shared `Connection` in `DatabaseConnection` (not a connection pool). All JDBC `ResultSet`s, `Statement`s, and `PreparedStatement`s must use try-with-resources to avoid resource leaks.

## Coding Conventions

### Logging
Use **Log4j2** with parameterized `{}` placeholders — never string concatenation:
```java
logger.info("User {} logged in from {}", username, ipAddress);
logger.error("Failed to load order {}: {}", orderId, e.getMessage(), e);
```

### Exception Handling
Custom exception classes (`AuthException`, `UserException`, `ServiceException`) extend `Exception` and provide two constructors:
```java
public MyException(String message) { super(message); }
public MyException(String message, Throwable cause) { super(message, cause); }
```

### Adding a New Feature (MVCS pattern)

Follow this layer order when adding a new domain object:

1. **Model** — create a POJO in `models/` with private fields and getters/setters.
2. **DAO** — create a class in `dao/` that uses `DatabaseConnection.getInstance()` for JDBC queries wrapped in try-with-resources.
3. **Service** — create a class in `services/` that calls the DAO and applies business logic; throw the appropriate custom exception on failure.
4. **Controller** — create a JavaFX controller in `controllers/` annotated with `@FXML` fields; bind it to a new FXML file under `resources/views/fxml/`.

### FXML Views
- FXML files are in `src/main/resources/views/fxml/`
- The shared admin stylesheet is `views/css/admin.css`
- Load views via `FXMLLoader` with the classpath resource path

## Common Tasks

### Check current application version
```bash
grep '<version>' pom.xml | head -1
# or
grep 'app.version' src/main/resources/config.properties
```

### Run with verbose logging
Set the log level in `src/main/resources/log4j2.xml` to `DEBUG` before running.

### Find all controllers
```bash
find src/main/java/com/farmiq/controllers -name "*.java"
```

### Find all FXML views
```bash
find src/main/resources/views/fxml -name "*.fxml"
```

## Troubleshooting

| Problem | Solution |
|---|---|
| `Communications link failure` | MySQL is not running or `db.host`/`db.port` is incorrect |
| `Unknown database 'farmiq'` | Run the database setup steps above |
| `Access denied for user` | Check `db.user` and `db.password` in config or env vars |
| JavaFX module errors on startup | Ensure Java 17+ is active: `java -version` |
| Build fails with `mvn clean compile` | Confirm Maven 3.8+ and Java 17+: `mvn -version` |
