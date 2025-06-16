CREATE TABLE language (
    code VARCHAR(5) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE app_user (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    date_joined DATE NOT NULL
);

CREATE TABLE exercise (
    id VARCHAR(36) PRIMARY KEY,
    met_value DOUBLE PRECISION NOT NULL,
    primary_muscle_group VARCHAR(255)
);

CREATE TABLE exercise_translation (
    exercise_id VARCHAR(36) NOT NULL REFERENCES exercise(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    instructions TEXT,
    PRIMARY KEY (exercise_id, language_code)
);

CREATE TABLE user_metric (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    date_recorded DATE NOT NULL,
    body_weight_kg DOUBLE PRECISION,
    height_cm DOUBLE PRECISION,
    UNIQUE(user_id, date_recorded)
);

CREATE TABLE exercise_session (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES app_user(id),
    exercise_id VARCHAR(36) NOT NULL REFERENCES exercise(id),
    session_date DATE NOT NULL,
    duration_minutes INT,
    notes TEXT
);

CREATE TABLE set_log (
    id VARCHAR(36) PRIMARY KEY,
    exercise_session_id VARCHAR(36) NOT NULL REFERENCES exercise_session(id) ON DELETE CASCADE,
    reps INT NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    rpe INT
);

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
   exercise_id VARCHAR(36) NOT NULL REFERENCES exercise(id),
   target_sets INT,
   target_reps_min INT,
   target_reps_max INT,
   target_rpe_min INT,
   target_rpe_max INT,
   rest_period_seconds INT,
   "order" INT NOT NULL
);

-- Ingredient and Product Tables

-- Gecentraliseerde categorieën/types
CREATE TABLE category (
    id VARCHAR(50) PRIMARY KEY
);

-- Vertalingen van categorieën
-- Als een categorie of taal wordt verwijderd, verdwijnen de vertalingen mee.
CREATE TABLE category_translation (
    category_id VARCHAR(50) NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (category_id, language_code)
);

-- Eenheden
CREATE TABLE unit (
    id VARCHAR(50) PRIMARY KEY
);

-- Vertalingen van eenheden
CREATE TABLE unit_translation (
    unit_id VARCHAR(50) NOT NULL REFERENCES unit(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    name_singular VARCHAR(50) NOT NULL,
    name_plural VARCHAR(50) NOT NULL,
    PRIMARY KEY (unit_id, language_code)
);

-- Ingrediënten
-- Als een categorie wordt verwijderd, worden alle ingrediënten in die categorie ook verwijderd.
CREATE TABLE ingredient (
    id VARCHAR(36) PRIMARY KEY,
    default_unit_id VARCHAR(50) NOT NULL REFERENCES unit(id)
);

CREATE TABLE ingredient_category (
    ingredient_id VARCHAR(36) NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    category_id VARCHAR(50) NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    PRIMARY KEY (ingredient_id, category_id)
);


-- Vertalingen van ingrediënten
-- Als een ingrediënt of taal wordt verwijderd, verdwijnen de vertalingen mee.
CREATE TABLE ingredient_translation (
    ingredient_id VARCHAR(36) NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (ingredient_id, language_code)
);

-- Producten
-- Als de eigenaar (user) wordt verwijderd, wordt de user_id op NULL gezet.
CREATE TABLE product (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES app_user(id) ON DELETE SET NULL,
    base_name VARCHAR(255) NOT NULL
);

-- Relatie tussen producten en categorieën
-- Bevat de details die kunnen veranderen
CREATE TABLE product_version (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    brand VARCHAR(255),
    category_id VARCHAR(50) NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    effective_from DATE NOT NULL,
    notes TEXT,
    UNIQUE(product_id, version_number)
);

-- Vertalingen van producten
-- Als een product of taal wordt verwijderd, verdwijnen de vertalingen mee.
CREATE TABLE product_version_translation (
    product_version_id VARCHAR(36) NOT NULL REFERENCES product_version(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_version_id, language_code)
);

-- Relatie tussen producten en ingrediënten
-- Als een product of ingrediënt wordt verwijderd, wordt de koppeling ook verwijderd.
CREATE TABLE product_ingredient (
    product_version_id VARCHAR(36) NOT NULL REFERENCES product_version(id) ON DELETE CASCADE,
    ingredient_id VARCHAR(36) NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    unit_id VARCHAR(50) NOT NULL REFERENCES unit(id),
    PRIMARY KEY (product_version_id, ingredient_id)
);

CREATE TABLE recipe (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES app_user(id) ON DELETE SET NULL,
    prep_time_minutes INT,
    cook_time_minutes INT,
    servings INT,
    created_at TIMESTAMP NOT NULL
);

-- Vertalingen voor recepten
-- Als een recept of taal wordt verwijderd, verdwijnen de vertalingen mee.
CREATE TABLE recipe_translation (
    recipe_id VARCHAR(36) NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY (recipe_id, language_code)
);

-- Ingrediënten die nodig zijn voor een recept
-- Als een recept of ingrediënt wordt verwijderd, wordt de koppeling ook verwijderd.
CREATE TABLE recipe_ingredient (
    id VARCHAR(36) PRIMARY KEY,
    recipe_id VARCHAR(36) NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    ingredient_id VARCHAR(36) NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    unit_id VARCHAR(50) NOT NULL REFERENCES unit(id)
);

CREATE TABLE recipe_ingredient_translation (
    recipe_ingredient_id VARCHAR(36) NOT NULL REFERENCES recipe_ingredient(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    notes TEXT NOT NULL,
    PRIMARY KEY (recipe_ingredient_id, language_code)
);

-- Bereidingsstappen voor een recept
-- Als een recept wordt verwijderd, worden alle stappen mee verwijderd.
CREATE TABLE recipe_step (
    id VARCHAR(36) PRIMARY KEY,
    recipe_id VARCHAR(36) NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    UNIQUE(recipe_id, step_number)
);

CREATE TABLE recipe_step_translation (
    recipe_step_id VARCHAR(36) NOT NULL REFERENCES recipe_step(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL REFERENCES language(code) ON DELETE CASCADE,
    instructions TEXT NOT NULL,
    PRIMARY KEY (recipe_step_id, language_code)
);

-- Logboek voor consumptie
CREATE TABLE consumption_log (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    consumed_at TIMESTAMP NOT NULL,
    notes TEXT
);

