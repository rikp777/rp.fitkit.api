-- Relatie tussen consumptie en producten
-- Beschermt historie: een product kan niet verwijderd worden als het al gelogd is.
CREATE TABLE consumption_item (
                                  id VARCHAR(36) PRIMARY KEY,
                                  consumption_log_id VARCHAR(36) NOT NULL REFERENCES consumption_log(id) ON DELETE CASCADE,
                                  serving_size DOUBLE PRECISION NOT NULL,
                                  unit_id VARCHAR(50) NOT NULL REFERENCES unit(id),

    -- Drie mogelijke links naar wat er geconsumeerd is
                                  product_version_id VARCHAR(36) REFERENCES product_version(id) ON DELETE RESTRICT,
                                  recipe_id VARCHAR(36) REFERENCES recipe(id) ON DELETE RESTRICT,
                                  ingredient_id VARCHAR(36) REFERENCES ingredient(id) ON DELETE RESTRICT,

    -- Zorgt ervoor dat precies één van de drie links is ingevuld
                                  CONSTRAINT chk_one_consumed_item CHECK (
                                      (CASE WHEN product_version_id IS NOT NULL THEN 1 ELSE 0 END) +
                                      (CASE WHEN recipe_id IS NOT NULL THEN 1 ELSE 0 END) +
                                      (CASE WHEN ingredient_id IS NOT NULL THEN 1 ELSE 0 END)
                                          = 1
                                      )
);

-- Ingrediënt vertalingen
CREATE OR REPLACE VIEW public.vw_ingredient_details AS
SELECT
    i.id AS ingredient_id,
    it.name AS ingredient_name,
    ut.name_singular AS unit_singular,
    ut.name_plural AS unit_plural,
    (SELECT STRING_AGG(ct.name, ', ')
     FROM ingredient_category ic
              JOIN category_translation ct ON ic.category_id = ct.category_id AND ct.language_code = it.language_code
     WHERE ic.ingredient_id = i.id
    ) AS categories
FROM
    ingredient i
        JOIN
    ingredient_translation it ON i.id = it.ingredient_id
        LEFT JOIN
    unit_translation ut ON i.default_unit_id = ut.unit_id AND it.language_code = ut.language_code
WHERE
    it.language_code = 'nl-NL';

-- Gedetailleerde product samenstelling
CREATE OR REPLACE VIEW vw_product_composition AS
SELECT
    p.base_name AS product_concept,
    pv.version_number,
    pvt.name AS product_version_name,
    pv.brand,
    it.name AS ingredient_name,
    pi.amount,
    ut.name_singular AS unit_name
FROM
    product_version pv
        JOIN
    product p ON pv.product_id = p.id
        JOIN
    product_ingredient pi ON pi.product_version_id = pv.id
        JOIN
    ingredient i ON pi.ingredient_id = i.id
        JOIN
    product_version_translation pvt ON pv.id = pvt.product_version_id
        LEFT JOIN
    ingredient_translation it ON it.ingredient_id = i.id AND it.language_code = pvt.language_code
        LEFT JOIN
    unit_translation ut ON ut.unit_id = pi.unit_id AND ut.language_code = pvt.language_code
WHERE
    pvt.language_code = 'nl-NL';

-- Compleet consumptieverslag per gebruiker
CREATE OR REPLACE VIEW vw_user_consumption_details AS
SELECT
    cl.user_id,
    au.username,
    cl.consumed_at,
    COALESCE(pvt.name, rt.name, it.name) AS consumed_item_name,
    CASE
        WHEN ci.product_version_id IS NOT NULL THEN 'Product'
        WHEN ci.recipe_id IS NOT NULL THEN 'Recipe'
        WHEN ci.ingredient_id IS NOT NULL THEN 'Ingredient'
        END AS item_type,
    ci.serving_size,
    ut.name_singular AS unit,
    pv.brand,
    cl.notes AS consumption_notes
FROM
    consumption_log cl
        JOIN
    app_user au ON cl.user_id = au.id
        JOIN
    consumption_item ci ON cl.id = ci.consumption_log_id
        LEFT JOIN
    unit_translation ut ON ci.unit_id = ut.unit_id AND ut.language_code = 'nl-NL'
        LEFT JOIN
    product_version pv ON ci.product_version_id = pv.id
        LEFT JOIN
    product_version_translation pvt ON pv.id = pvt.product_version_id AND pvt.language_code = ut.language_code
        LEFT JOIN
    recipe_translation rt ON ci.recipe_id = rt.recipe_id AND rt.language_code = ut.language_code
        LEFT JOIN
    ingredient_translation it ON ci.ingredient_id = it.ingredient_id AND it.language_code = ut.language_code
ORDER BY
    au.username, cl.consumed_at DESC;

-- Gedetailleerd workout-logboek
CREATE OR REPLACE VIEW vw_user_workout_log AS
SELECT
    au.username,
    es.session_date,
    et.name AS exercise_name,
    sl.reps,
    sl.weight,
    sl.rpe,
    es.notes
FROM
    exercise_session es
        JOIN
    app_user au ON es.user_id = au.id
        JOIN
    set_log sl ON sl.exercise_session_id = es.id
        LEFT JOIN
    exercise_translation et ON es.exercise_id = et.exercise_id
WHERE
    et.language_code = 'nl-NL'
ORDER BY
    au.username, es.session_date DESC, sl.id;

-- Voortgang per Oefening (Progressive Overload)
CREATE OR REPLACE VIEW vw_exercise_progression AS
SELECT
    es.user_id,
    et.name AS exercise_name,
    es.session_date,
    SUM(sl.reps * sl.weight) AS total_volume,
    MAX(sl.weight) AS max_weight,
    MAX(sl.weight * (1 + (sl.reps / 30.0))) AS estimated_1rm
FROM
    exercise_session es
        JOIN
    set_log sl ON es.id = sl.exercise_session_id
        JOIN
    exercise_translation et ON es.exercise_id = et.exercise_id
WHERE
    et.language_code = 'nl-NL'
GROUP BY
    es.user_id,
    et.name,
    es.session_date
ORDER BY
    es.user_id,
    et.name,
    es.session_date;

-- Dagelijkse inname van Ingrediënten
CREATE OR REPLACE VIEW vw_daily_ingredient_intake AS
WITH daily_consumption AS (
    -- Deel 1: Ingrediënten uit geconsumeerde producten
    SELECT
        cl.user_id,
        DATE(cl.consumed_at) AS consumption_date,
        pi.ingredient_id,
        (pi.amount * ci.serving_size) AS total_amount
    FROM
        consumption_log cl
            JOIN
        consumption_item ci ON cl.id = ci.consumption_log_id
            JOIN
        product_ingredient pi ON ci.product_version_id = pi.product_version_id
    WHERE
        ci.product_version_id IS NOT NULL

    UNION ALL

    -- Deel 2: Ingrediënten uit geconsumeerde recepten
    SELECT
        cl.user_id,
        DATE(cl.consumed_at) AS consumption_date,
        ri.ingredient_id,
        (ri.amount * ci.serving_size) AS total_amount
    FROM
        consumption_log cl
            JOIN
        consumption_item ci ON cl.id = ci.consumption_log_id
            JOIN
        recipe_ingredient ri ON ci.recipe_id = ri.recipe_id
    WHERE
        ci.recipe_id IS NOT NULL

    UNION ALL

    -- Deel 3: Direct geconsumeerde losse ingrediënten
    SELECT
        cl.user_id,
        DATE(cl.consumed_at) AS consumption_date,
        ci.ingredient_id,
        ci.serving_size AS total_amount
    FROM
        consumption_log cl
            JOIN
        consumption_item ci ON cl.id = ci.consumption_log_id
    WHERE
        ci.ingredient_id IS NOT NULL
)
SELECT
    user_id,
    consumption_date,
    ingredient_id,
    SUM(total_amount) AS total_daily_amount
FROM
    daily_consumption
GROUP BY
    user_id, consumption_date, ingredient_id
ORDER BY
    user_id, consumption_date, ingredient_id;

-- Populairste Producten
CREATE OR REPLACE VIEW vw_most_popular_products AS
SELECT
    pvt.name AS product_name,
    pv.brand,
    COUNT(ci.id) AS log_count,
    p.id AS product_id,
    pv.id AS product_version_id
FROM consumption_item ci
         JOIN product_version pv ON ci.product_version_id = pv.id
         JOIN product p ON pv.product_id = p.id
         JOIN product_version_translation pvt ON pvt.product_version_id = pv.id
WHERE pvt.language_code = 'nl-NL'
GROUP BY p.id, pv.id, pvt.name, pv.brand
ORDER BY log_count DESC;


CREATE OR REPLACE VIEW vw_recipe_full_details AS
SELECT
    r.id AS recipe_id,
    rt.language_code,
    rt.name AS recipe_name,
    rt.description,
    r.prep_time_minutes,
    r.cook_time_minutes,
    r.servings,
    -- Ingrediënt details
    it.name AS ingredient_name,
    ri.amount AS ingredient_amount,
    ut.name_singular AS ingredient_unit,
    rit.notes AS ingredient_notes,
    -- Stap details
    rs.step_number,
    rst.instructions
FROM
    recipe r
        JOIN
    recipe_translation rt ON r.id = rt.recipe_id
        LEFT JOIN
    recipe_ingredient ri ON r.id = ri.recipe_id
        LEFT JOIN
    ingredient_translation it ON ri.ingredient_id = it.ingredient_id AND it.language_code = rt.language_code
        LEFT JOIN
    unit_translation ut ON ri.unit_id = ut.unit_id AND ut.language_code = rt.language_code
        LEFT JOIN
    recipe_ingredient_translation rit ON ri.id = rit.recipe_ingredient_id AND rit.language_code = rt.language_code
        LEFT JOIN
    recipe_step rs ON r.id = rs.recipe_id
        LEFT JOIN
    recipe_step_translation rst ON rs.id = rst.recipe_step_id AND rst.language_code = rt.language_code
WHERE
    rt.language_code = 'nl-NL'
ORDER BY
    r.id, rs.step_number;


CREATE OR REPLACE VIEW vw_session_calorie_expenditure AS
WITH latest_user_weight AS (
    SELECT DISTINCT ON (es.id)
        es.id AS session_id,
        um.body_weight_kg
    FROM exercise_session es
             JOIN user_metric um ON es.user_id = um.user_id AND um.date_recorded <= es.session_date
    ORDER BY es.id, um.date_recorded DESC
)
SELECT
    es.id AS session_id,
    es.user_id,
    es.session_date,
    et.name AS exercise_name,
    es.duration_minutes,
    luw.body_weight_kg,
    e.met_value,
    (e.met_value * 3.5 * luw.body_weight_kg / 200) * es.duration_minutes AS estimated_calories_burned
FROM
    exercise_session es
        JOIN
    exercise e ON es.exercise_id = e.id
        LEFT JOIN
    latest_user_weight luw ON es.id = luw.session_id
        LEFT JOIN
    exercise_translation et ON e.id = et.exercise_id AND et.language_code = 'nl-NL'
WHERE
    es.duration_minutes IS NOT NULL AND luw.body_weight_kg IS NOT NULL;