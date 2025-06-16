-- Logboek voor consumpties
INSERT INTO consumption_log (id, user_id, consumed_at, notes) VALUES
                                                                  ('664da985-0965-4d28-9a13-f4d6d31e26a5', '6ca7de68-de98-4d1e-b304-c01660a1654b', '2023-05-10 18:30:00', 'Post-workout shake van vorig jaar.'),
                                                                  ('5e1c19ce-1a12-4f97-84b9-38b843986a16', '6ca7de68-de98-4d1e-b304-c01660a1654b', '2023-05-11 08:00:00', 'Shake van vanochtend.'),
                                                                  ('dd155f73-6663-4b77-9deb-a2842a878ae8', '6ca7de68-de98-4d1e-b304-c01660a1654b', '2024-06-15 19:00:00', 'Avondeten gisteren.');

-- Items van de consumpties (nu met NULL voor de ongebruikte kolommen)
INSERT INTO consumption_item (id, consumption_log_id, product_version_id, recipe_id, ingredient_id, serving_size, unit_id) VALUES
                                                                                                                               ('140f1871-2cda-4f61-a41c-8f57b0e97cca', '664da985-0965-4d28-9a13-f4d6d31e26a5', 'pv-whey-v1', NULL, NULL, 1, 'scoop'),
                                                                                                                               ('23176952-ddda-431d-bb15-41ad9fcfe15d', '5e1c19ce-1a12-4f97-84b9-38b843986a16', 'pv-whey-v2', NULL, NULL, 1, 'scoop');

-- Logboek voor nieuwe consumpties
INSERT INTO consumption_log (id, user_id, consumed_at, notes) VALUES
                                                                  ('0f5dcdfc-3a9d-473c-848e-fca4534081df', '6ca7de68-de98-4d1e-b304-c01660a1654b', '2025-06-16 12:30:00', 'Net het nieuwe recept gegeten.'),
                                                                  ('2194e71f-60dd-4d2e-8791-f061cc88fe01', '6ca7de68-de98-4d1e-b304-c01660a1654b', '2025-06-16 15:00:00', 'Snelle snack.');

-- De bijbehorende items
INSERT INTO consumption_item (id, consumption_log_id, product_version_id, recipe_id, ingredient_id, serving_size, unit_id) VALUES
-- Voorbeeld 1: Consumeer 1 portie van het recept 'rec-kip-rijst-basis'
('a56103e8-baad-4863-82c3-dc59613f0fec', '0f5dcdfc-3a9d-473c-848e-fca4534081df', NULL, 'rec-kip-rijst-basis', NULL, 1, 'portie'),
-- Voorbeeld 2: Consumeer 1 losse banaan
('c271e7ea-a27d-4c86-9dbd-fde570a5e2d3', '2194e71f-60dd-4d2e-8791-f061cc88fe01', NULL, NULL, 'ing-pro-banaan', 1, 'stuk');






INSERT INTO workout_plan (id, user_id, name, description, is_active) VALUES
    ('plan-1-uuid', '6ca7de68-de98-4d1e-b304-c01660a1654b', 'Push/Pull/Legs', 'Klassiek PPL schema voor kracht', true);

INSERT INTO workout_template (id, workout_plan_id, name, day_of_week) VALUES
                                                                          ('template-push-uuid', 'plan-1-uuid', 'Push Dag', 1),
                                                                          ('template-pull-uuid', 'plan-1-uuid', 'Pull Dag', 2);

INSERT INTO exercise_template (id, workout_template_id, exercise_id, target_sets, target_reps_min, target_reps_max, target_rpe_min, target_rpe_max, rest_period_seconds, "order") VALUES
                                                                                                                                                                                      ('ex-template-bench', 'template-push-uuid', 'ex-bench-press', 4, 6, 10, 7, 9, 120, 1),
                                                                                                                                                                                      ('ex-template-ohp', 'template-push-uuid', 'ex-overhead-press', 3, 8, 12, 8, 10, 90, 2);

INSERT INTO exercise_session (id, user_id, exercise_id, session_date, duration_minutes, notes) VALUES
    ('session-1-uuid', '6ca7de68-de98-4d1e-b304-c01660a1654b', 'ex-bench-press', '2023-05-10', 45, 'Voelde goed, focus op techniek.');

INSERT INTO set_log (id, exercise_session_id, reps, weight, rpe) VALUES
                                                                     ('set-1-uuid', 'session-1-uuid', 10, 60, 7),
                                                                     ('set-2-uuid', 'session-1-uuid', 9, 60, 8),
                                                                     ('set-3-uuid', 'session-1-uuid', 8, 60, 8);