# FarmIQ — GitHub Copilot Custom Instructions

These instructions are automatically loaded by GitHub Copilot (free or paid) to give it context about the FarmIQ project. No API key or paid subscription beyond the free Copilot plan is needed.

## Project Overview

FarmIQ is a **JavaFX 21 + Java 17 + MySQL** desktop application built with Maven. It is a back-office farm management system for the Tunisian market (locale `fr_TN`, currency `TND`).

## Build & Run

```bash
mvn clean compile          # compile
mvn javafx:run             # run the app
mvn clean package -DskipTests  # package as JAR
./run.sh                   # Linux/macOS shortcut
run.bat                    # Windows shortcut
```

## Package Structure

```
com.farmiq
├── Main.java              # entry point
├── controllers/           # JavaFX @FXML controllers
├── dao/                   # JDBC data-access objects
├── exceptions/            # AuthException, UserException, ServiceException
├── models/                # domain POJOs + models/enums/
├── services/              # business logic
└── utils/                 # DatabaseConnection, helpers
```

FXML views → `src/main/resources/views/fxml/`
CSS        → `src/main/resources/views/css/admin.css`
Config     → `src/main/resources/config.properties`

## Configuration

`config.properties` values can be overridden by environment variables:

| Property | Env var |
|---|---|
| `db.host` | `DB_HOST` |
| `db.user` | `DB_USER` |
| `db.password` | `DB_PASSWORD` |
| `mail.host` | `MAIL_HOST` |

## Coding Conventions

### Logging — Log4j2 with `{}` placeholders
```java
logger.info("User {} logged in", username);
logger.error("Failed to save order {}: {}", id, e.getMessage(), e);
```

### Custom Exceptions — two constructors
```java
public MyException(String message) { super(message); }
public MyException(String message, Throwable cause) { super(message, cause); }
```

### New Feature Pattern (MVCS)
1. **Model** — POJO in `models/`
2. **DAO** — JDBC class in `dao/` using `DatabaseConnection.getInstance()` with try-with-resources for `ResultSet`, `Statement`, and `PreparedStatement` objects (do **not** close the shared `Connection` — it is a singleton managed by `DatabaseConnection`)
3. **Service** — business logic in `services/`, throws the appropriate custom exception
4. **Controller** — `@FXML` controller in `controllers/` bound to a new FXML file

### Database
- Single shared `Connection` via `DatabaseConnection` (no pool)
- Always wrap JDBC resources in try-with-resources to avoid leaks
- MySQL 8+, database name `farmiq`, default port `3306`
