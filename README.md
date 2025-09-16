# FitKit API

Welcome to the FitKit API! This is a personal project designed for learning and experimenting with modern web technologies, specifically focused on Spring Boot with WebFlux and R2DBC. The goal is to build the backend for a fitness and meal-tracking application.

## ✨ Core Features
- **Digital Logbook:** The central feature is a logbook where users can record daily entries (DailyLog).
- **Interconnected Notes:** Entries can be linked to one another using a simple [anchor](log:id) syntax, creating a network of thoughts and activities.
- **Backlink Tracking:** The API automatically tracks incoming links, allowing users to see which other entries reference the current one.

## 🛠️ Technology Stack

This project was created to learn and explore the following technologies:
- **Language:** Java (SDK 21)
- **Framework:** Spring Boot with Spring WebFlux (for reactive programming)
- **Database:** PostgreSQL with R2DBC (for reactive, non-blocking database access)
- **Build Tool:** Maven
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)

## 📄 API Documentation

The API is fully documented using Swagger UI. You can explore and test all available endpoints interactively after running the application locally.
- Swagger UI: http://localhost:6078/swagger-ui/index.html
- OpenAPI JSON: http://localhost:6078/v3/api-docs

## 🔑 Authentication

This project uses a self-managed, JWT-based authentication system. A design decision was made to avoid using external OAuth2 providers (e.g., Google, Microsoft).

## ⚠️ Important Development Note: Database Seeding

When you manually insert test data (e.g., using Liquibase's <loadData> or raw SQL scripts) and provide explicit primary key values for tables with auto-incrementing columns, you must also manually update PostgreSQL's sequence counter.

The database does exactly what you tell it to do: it inserts the data with the ID you provided. However, this process bypasses the sequence, and its internal counter does not get updated. This will cause duplicate key errors later when the application tries to insert a new row, as the sequence will try to generate an ID that already exists.

After loading your data, you must run a command to synchronize the sequence. The best way to do this is to add a <sql> block at the end of your Liquibase data-loading changeset.

### Example Command:
```SQL

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
```

This will ensure the application can generate new IDs without conflict.