# FarmIQ — AgriTech Platform

**FarmIQ** is a desktop agricultural management platform built for Tunisian farmers, administrators, and agricultural technicians. It covers farm plot management, crop tracking, marketplace, inventory, orders, transactions, and more.

---

## Tech Stack

| Layer        | Technology                     |
|-------------|--------------------------------|
| Language     | Java 17                        |
| UI Framework | JavaFX 21.0.5 (FXML + CSS)   |
| Database     | MySQL 8.3.0                   |
| Build        | Maven 3                       |
| Security     | BCrypt (jBCrypt 0.4)          |
| Logging      | Log4j 2.22.1                  |
| Email        | JavaMail 1.6.2 (SMTP)        |
| PDF Export   | iTextPDF 5.5.13.3             |
| QR Codes     | Google ZXing 3.5.2            |
| JSON         | Gson 2.10.1                   |
| Testing      | JUnit 5.10.1                  |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    JavaFX UI                         │
│            (FXML Views + CSS Styling)                │
├─────────────────────────────────────────────────────┤
│                  Controllers                         │
│   Login, Dashboard, Admin, Marketplace, Calendar...  │
├─────────────────────────────────────────────────────┤
│                   Services                           │
│  Auth, User, Order, Cart, Listing, Product,          │
│  Transaction, Notification, Weather, Seller...       │
├─────────────────────────────────────────────────────┤
│                  DAO Layer                            │
│     User, Role, Permission, Transaction,             │
│     Parcelle, Plante DAOs                            │
├─────────────────────────────────────────────────────┤
│                  Utils                                │
│  DatabaseConnection, SessionManager, Validation,     │
│  Password, Navigation, Alert, Currency, Image        │
├─────────────────────────────────────────────────────┤
│                  Models                               │
│  User, Role, Permission, Order, Product, Listing,    │
│  Transaction, Parcelle, Plante, Notification...      │
├─────────────────────────────────────────────────────┤
│                  MySQL Database                       │
└─────────────────────────────────────────────────────┘
```

**Pattern**: MVC + DAO + Service layer

- **Views** — FXML files with CSS styling
- **Controllers** — Handle UI events and delegate to services
- **Services** — Business logic and orchestration
- **DAOs** — Database access via JDBC PreparedStatements
- **Models** — POJOs with enums for status types
- **Utils** — Cross-cutting utilities (DB, session, validation, etc.)

---

## Project Structure

```
src/main/java/com/farmiq/
├── Main.java                     # JavaFX entry point
├── controllers/                  # 15 UI controllers
├── services/                     # 16 business services
├── dao/                          # 6 data access objects
├── models/                       # 18 model classes + enums
├── utils/                        # 8 utility classes
└── exceptions/                   # ServiceException, AuthException, UserException

src/main/resources/
├── config.properties             # App configuration
├── log4j2.xml                    # Logging configuration
└── views/
    ├── fxml/                     # 16 FXML view files
    └── css/                      # Stylesheets

sql/                              # Database migration scripts
```

---

## Prerequisites

- **Java 17** or later
- **MySQL 8.0+** running locally
- **Maven 3.6+**

## Setup

1. **Create the database**:
   ```bash
   mysql -u root -p < sql/farmiq.sql
   mysql -u root -p farmiq < sql/database_setup.sql
   mysql -u root -p farmiq < sql/add_missing_tables.sql
   mysql -u root -p farmiq < sql/transactions_setup.sql
   mysql -u root -p farmiq < sql/migrations_v3.sql
   mysql -u root -p farmiq < sql/add_user_to_transactions.sql
   ```

2. **Configure** `src/main/resources/config.properties`:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=farmiq
   db.user=root
   db.password=yourpassword
   ```
   Or use environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

3. **Build & Run**:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```
   Or use the provided scripts:
   ```bash
   ./run.sh main    # Linux/macOS
   run.bat           # Windows
   ```

---

## Configuration

All configuration is in `src/main/resources/config.properties`. Every setting can be overridden via environment variables for production deployment:

| Config Key         | Env Variable     | Default          | Description                |
|-------------------|-----------------|------------------|----------------------------|
| `db.host`         | `DB_HOST`       | `localhost`      | Database host              |
| `db.port`         | `DB_PORT`       | `3306`           | Database port              |
| `db.name`         | `DB_NAME`       | `farmiq`         | Database name              |
| `db.user`         | `DB_USER`       | `root`           | Database user              |
| `db.password`     | `DB_PASSWORD`   | (empty)          | Database password          |
| `mail.host`       | `MAIL_HOST`     | `smtp.gmail.com` | SMTP host                  |
| `mail.port`       | `MAIL_PORT`     | `587`            | SMTP port                  |
| `mail.user`       | `MAIL_USER`     | (empty)          | SMTP username              |
| `mail.password`   | `MAIL_PASSWORD` | (empty)          | SMTP password              |
| `app.images.dir`  | `APP_IMAGES_DIR`| `./images`       | Image storage directory    |

---

## Roles & Permissions

| Role          | Description                          |
|--------------|--------------------------------------|
| ADMIN        | Full access — user management, system settings |
| AGRICULTEUR  | Farmer — parcels, crops, marketplace, orders    |
| TECHNICIEN   | Technician — tasks, parcels, monitoring         |

---

## Key Features

- **Authentication** — BCrypt password hashing, session management
- **Farm Management** — Parcels (parcelles), crops (plantes), soil types
- **Marketplace** — Listings, cart, orders, seller profiles, reviews
- **Inventory** — Products, suppliers (fournisseurs), stock alerts
- **Transactions** — Financial tracking with CSV/PDF export
- **Notifications** — In-app alerts for orders, stock, status changes
- **Calendar** — Task management with priority and deadlines
- **Weather** — Live weather via Open-Meteo API (free, no API key)
- **Dashboard** — Statistics and overview charts
- **Email** — Password reset via SMTP (JavaMail)
- **Image Storage** — Local file storage for profile photos

---

## Technical Audit Summary

### Strengths
- ✅ BCrypt password hashing (12 rounds)
- ✅ Parameterized SQL queries throughout (no SQL injection)
- ✅ Input validation (email, name, password strength)
- ✅ Structured logging with Log4j
- ✅ Try-with-resources for all database resources
- ✅ RBAC (Role-Based Access Control)
- ✅ Clean separation of concerns (Controller → Service → DAO)
- ✅ Environment variable overrides for production config

### Improvements Made
- 🔧 Replaced hardcoded API keys with config-based loading
- 🔧 Replaced paid APIs (SendGrid, Cloudinary) with free alternatives (SMTP, local storage)
- 🔧 Fixed all ResultSet resource leaks across 6 service classes
- 🔧 Added environment variable support for database and email configuration
- 🔧 Replaced empty catch blocks with proper logging
- 🔧 Added centralized ServiceException for consistent error handling

### Known Limitations
- Single shared DB connection (not a connection pool)
- No automated tests yet (JUnit configured but no test classes)
- Desktop-only (no REST API layer for mobile/web clients)

---

## License

This project is for educational and demonstration purposes.
