# FitKit API Documentatie
Welkom bij de FitKit API! Deze API is het hart van mijn persoonlijke project om te leren en te experimenteren met moderne webtechnologieën, specifiek gericht op Spring Boot met WebFlux en R2DBC. Het is een hobbyproject om mijn vaardigheden te verbeteren en plezier te hebben met coderen.

## Wat is FitKit?
FitKit is een fitness- en maaltijdapp in ontwikkeling. Het doel is om gebruikers te helpen met het bijhouden van hun workouts, maaltijden, en algemene voortgang op hun fitnessreis. De API is verantwoordelijk voor het beheren van alle data die hiervoor nodig is.

## Waarom deze API?
Dit project is puur uit interesse en de drang om te leren ontstaan. Ik wil graag dieper duiken in:

- Reactive Programming met Spring WebFlux: Hoe bouw ik schaalbare en responsieve applicaties?
- Non-blocking database toegang met R2DBC: Hoe integreer ik dit efficiënt met een PostgreSQL database?

## API Documentatie (Swagger UI)
Om de API te verkennen en te testen, kun je de interactieve Swagger UI gebruiken. Hier vind je alle beschikbare endpoints, hun functionaliteit en de vereiste parameters.

### Lokale ontwikkelomgeving:
Als de API lokaal draait:
- Swagger UI: http://localhost:6078/swagger-ui/index.html
- OpenAPI JSON: http://localhost:6078/v3/api-docs

#Notes
Don't use oauth2 of organistaion like google or microsoft because I hate them. 

#Notes
Don't use oauth2 of organistaion like google or microsoft because I hate them.

## Important Note on Database Seeding

When you manually insert test data (e.g., using Liquibase's `<loadData>` or raw SQL scripts) and **provide explicit primary key values** for tables with auto-incrementing columns, you must also manually update PostgreSQL's sequence counter.

**Why?** The database does exactly what you tell it to do: it inserts the data with the ID you provided. However, this process bypasses the sequence, and its internal counter does not get updated. This will cause `duplicate key` errors later when the application tries to insert a new row, as the sequence will try to generate an ID that already exists.

**Solution:** After loading your data, you must run a command to synchronize the sequence. The best way to do this is to add a `<sql>` block at the end of your Liquibase data-loading changeset.

**Example Command:**
I added one in the `db/changelog/changesets/insert-logbook-data.xml` file for the `users` table:

```sql 
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
```

This will ensure the application can generate new IDs without conflict.
