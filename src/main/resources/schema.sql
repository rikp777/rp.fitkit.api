DROP TABLE IF EXISTS set_log;
DROP TABLE IF EXISTS exercise_session;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
  id VARCHAR(36) PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  date_joined DATE NOT NULL
);


CREATE TABLE exercise_session (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL REFERENCES app_user(id),
  exercise_name VARCHAR(255) NOT NULL,
  session_date DATE NOT NULL,
  notes TEXT
);

CREATE TABLE set_log (
 id VARCHAR(36) PRIMARY KEY,
 exercise_session_id VARCHAR(36) NOT NULL REFERENCES exercise_session(id) ON DELETE CASCADE,
 reps INT NOT NULL,
 weight DOUBLE PRECISION NOT NULL,
 rpe INT
);

INSERT INTO app_user (id, username, email, password_hash, date_joined) VALUES
    ('user123', 'testuser', 'test@example.com', '$2a$10$G3JpSg9gT9oXg.X2cZ8.i.eNo8hBScn3pB3uA.i.3jY9K9Xl/qE0u', CURRENT_DATE);

CREATE TABLE workout_plan (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_active BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE workout_template (
  id VARCHAR(36) PRIMARY KEY,
  workout_plan_id VARCHAR(36) NOT NULL REFERENCES workout_plan(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  day_of_week INT
);

CREATE TABLE exercise_template (
   id VARCHAR(36) PRIMARY KEY,
   workout_template_id VARCHAR(36) NOT NULL REFERENCES workout_template(id) ON DELETE CASCADE,
   exercise_name VARCHAR(255) NOT NULL,
   target_sets INT,
   target_reps_min INT,
   target_reps_max INT,
   target_rpe_min INT,
   target_rpe_max INT,
   rest_period_seconds INT,
   "order" INT NOT NULL
);
