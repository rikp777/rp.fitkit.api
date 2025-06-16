-- Voeg het basisrecept toe
INSERT INTO recipe (id, user_id, prep_time_minutes, cook_time_minutes, servings, created_at) VALUES
    ('rec-kip-rijst-basis', '6ca7de68-de98-4d1e-b304-c01660a1654b', 10, 20, 2, '2025-06-16 12:00:00');

-- Voeg de vertalingen voor het recept toe
INSERT INTO recipe_translation (recipe_id, language_code, name, description) VALUES
                                                                                 ('rec-kip-rijst-basis', 'nl-NL', 'Simpele Kip met Rijst', 'Een snelle en eiwitrijke maaltijd, perfect voor na het sporten.'),
                                                                                 ('rec-kip-rijst-basis', 'en-US', 'Simple Chicken with Rice', 'A quick and protein-rich meal, perfect for post-workout.');

-- Voeg de ingrediënten toe
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, amount, unit_id) VALUES
                                                                                  ('rec-ing-1', 'rec-kip-rijst-basis', 'ing-pro-kip', 300, 'g'),
                                                                                  ('rec-ing-2', 'rec-kip-rijst-basis', 'ing-pro-rijst', 150, 'g'),
                                                                                  ('rec-ing-3', 'rec-kip-rijst-basis', 'ing-pro-ui', 1, 'stuk'),
                                                                                  ('rec-ing-4', 'rec-kip-rijst-basis', 'ing-pro-knoflook', 2, 'teen'),
                                                                                  ('rec-ing-5', 'rec-kip-rijst-basis', 'ing-pro-olie', 15, 'ml'),
                                                                                  ('rec-ing-6', 'rec-kip-rijst-basis', 'ing-sea-zout', 2, 'g'),
                                                                                  ('rec-ing-7', 'rec-kip-rijst-basis', 'ing-sea-zwartepeper', 1, 'g');

-- Voeg de vertaalde notities voor de ingrediënten toe
INSERT INTO recipe_ingredient_translation (recipe_ingredient_id, language_code, notes) VALUES
                                                                                           ('rec-ing-1', 'nl-NL', 'in blokjes gesneden'),
                                                                                           ('rec-ing-1', 'en-US', 'cut into cubes'),
                                                                                           ('rec-ing-2', 'nl-NL', 'ongekookt'),
                                                                                           ('rec-ing-2', 'en-US', 'uncooked'),
                                                                                           ('rec-ing-3', 'nl-NL', 'gesnipperd'),
                                                                                           ('rec-ing-3', 'en-US', 'chopped'),
                                                                                           ('rec-ing-4', 'nl-NL', 'fijngehakt'),
                                                                                           ('rec-ing-4', 'en-US', 'minced'),
                                                                                           ('rec-ing-6', 'nl-NL', 'naar smaak'),
                                                                                           ('rec-ing-6', 'en-US', 'to taste'),
                                                                                           ('rec-ing-7', 'nl-NL', 'naar smaak'),
                                                                                           ('rec-ing-7', 'en-US', 'to taste');


-- Voeg de stappen toe
INSERT INTO recipe_step (id, recipe_id, step_number) VALUES
                                                         ('rec-step-1', 'rec-kip-rijst-basis', 1),
                                                         ('rec-step-2', 'rec-kip-rijst-basis', 2),
                                                         ('rec-step-3', 'rec-kip-rijst-basis', 3),
                                                         ('rec-step-4', 'rec-kip-rijst-basis', 4),
                                                         ('rec-step-5', 'rec-kip-rijst-basis', 5),
                                                         ('rec-step-6', 'rec-kip-rijst-basis', 6);

-- Voeg de vertaalde instructies voor de stappen toe
INSERT INTO recipe_step_translation (recipe_step_id, language_code, instructions) VALUES
                                                                                      ('rec-step-1', 'nl-NL', 'Kook de rijst volgens de aanwijzingen op de verpakking.'),
                                                                                      ('rec-step-1', 'en-US', 'Cook the rice according to the package directions.'),
                                                                                      ('rec-step-2', 'nl-NL', 'Verhit de olijfolie in een grote koekenpan of wok op middelhoog vuur.'),
                                                                                      ('rec-step-2', 'en-US', 'Heat the olive oil in a large skillet or wok over medium-high heat.'),
                                                                                      ('rec-step-3', 'nl-NL', 'Voeg de gesnipperde ui toe en bak tot deze glazig is. Voeg daarna de knoflook toe en bak 1 minuut mee.'),
                                                                                      ('rec-step-3', 'en-US', 'Add the chopped onion and sauté until translucent. Then add the garlic and sauté for another minute.'),
                                                                                      ('rec-step-4', 'nl-NL', 'Voeg de kipblokjes toe aan de pan. Bak ze goudbruin en gaar.'),
                                                                                      ('rec-step-4', 'en-US', 'Add the chicken cubes to the pan. Cook until golden brown and cooked through.'),
                                                                                      ('rec-step-5', 'nl-NL', 'Breng op smaak met zout en peper.'),
                                                                                      ('rec-step-5', 'en-US', 'Season with salt and pepper.'),
                                                                                      ('rec-step-6', 'nl-NL', 'Serveer de kip naast de gekookte rijst. Voeg eventueel groenten naar keuze toe.'),
                                                                                      ('rec-step-6', 'en-US', 'Serve the chicken alongside the cooked rice. Add vegetables of your choice if desired.');

