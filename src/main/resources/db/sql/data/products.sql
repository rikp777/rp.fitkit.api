-- Product: Gold Standard Whey
INSERT INTO product (id, user_id, base_name) VALUES
    ('prod-whey-concept', NULL, '100% Whey Proteïne');
-- V1
INSERT INTO product_version (id, product_id, version_number, brand, category_id, effective_from, notes) VALUES
    ('pv-whey-v1', 'prod-whey-concept', 1, 'Gold Standard', 'SUPPLEMENT', '2022-01-01', 'Originele formule');
INSERT INTO product_version_translation (product_version_id, language_code, name) VALUES
                                                                                      ('pv-whey-v1', 'nl-NL', '100% Whey Proteïne'),
                                                                                      ('pv-whey-v1', 'en-US', '100% Whey Protein');
INSERT INTO product_ingredient (product_version_id, ingredient_id, amount, unit_id) VALUES
                                                                                        ('pv-whey-v1', 'ing-macro-whey', 30, 'g'),
                                                                                        ('pv-whey-v1', 'ing-suiker', 5, 'g');
-- V2
INSERT INTO product_version (id, product_id, version_number, brand, category_id, effective_from, notes) VALUES
    ('pv-whey-v2', 'prod-whey-concept', 2, 'Gold Standard', 'SUPPLEMENT', '2024-01-01', 'Nieuwe, verbeterde formule');
INSERT INTO product_version_translation (product_version_id, language_code, name) VALUES
                                                                                      ('pv-whey-v2', 'nl-NL', '100% Whey (Nieuwe Formule)'),
                                                                                      ('pv-whey-v2', 'en-US', '100% Whey (New Formula)');
INSERT INTO product_ingredient (product_version_id, ingredient_id, amount, unit_id) VALUES
    ('pv-whey-v2', 'ing-macro-whey', 22, 'g');