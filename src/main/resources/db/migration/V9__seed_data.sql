-- =============================================================
-- V9 - Seed data (datos de prueba)
-- Orden: country → classification → category → actor → people
--        → movie → movie_country_info → poster → movie_categories
--        → cast_movie → movie_people
-- =============================================================

-- -------------------------
-- COUNTRIES
-- -------------------------
INSERT INTO country (id, name) VALUES
    ('aaaaaaaa-0001-0001-0001-000000000001', 'United States'),
    ('aaaaaaaa-0001-0001-0001-000000000002', 'Mexico'),
    ('aaaaaaaa-0001-0001-0001-000000000003', 'Spain');

-- -------------------------
-- CLASSIFICATIONS
-- (una por país, age_limit en años)
-- -------------------------
INSERT INTO classification (id, country_id, name, age_limit) VALUES
    -- USA
    ('bbbbbbbb-0002-0002-0002-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', 'G',     0),
    ('bbbbbbbb-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', 'PG',    8),
    ('bbbbbbbb-0002-0002-0002-000000000003', 'aaaaaaaa-0001-0001-0001-000000000001', 'PG-13', 13),
    ('bbbbbbbb-0002-0002-0002-000000000004', 'aaaaaaaa-0001-0001-0001-000000000001', 'R',     17),
    -- Mexico
    ('bbbbbbbb-0002-0002-0002-000000000005', 'aaaaaaaa-0001-0001-0001-000000000002', 'AA',    0),
    ('bbbbbbbb-0002-0002-0002-000000000006', 'aaaaaaaa-0001-0001-0001-000000000002', 'A',     0),
    ('bbbbbbbb-0002-0002-0002-000000000007', 'aaaaaaaa-0001-0001-0001-000000000002', 'B',     12),
    ('bbbbbbbb-0002-0002-0002-000000000008', 'aaaaaaaa-0001-0001-0001-000000000002', 'C',     18),
    -- Spain
    ('bbbbbbbb-0002-0002-0002-000000000009', 'aaaaaaaa-0001-0001-0001-000000000003', 'APTA',  0),
    ('bbbbbbbb-0002-0002-0002-000000000010', 'aaaaaaaa-0001-0001-0001-000000000003', '7',     7),
    ('bbbbbbbb-0002-0002-0002-000000000011', 'aaaaaaaa-0001-0001-0001-000000000003', '12',    12),
    ('bbbbbbbb-0002-0002-0002-000000000012', 'aaaaaaaa-0001-0001-0001-000000000003', '16',    16);

-- -------------------------
-- CATEGORIES
-- -------------------------
INSERT INTO category (id, name) VALUES
    ('cccccccc-0003-0003-0003-000000000001', 'Action'),
    ('cccccccc-0003-0003-0003-000000000002', 'Drama'),
    ('cccccccc-0003-0003-0003-000000000003', 'Sci-Fi'),
    ('cccccccc-0003-0003-0003-000000000004', 'Thriller'),
    ('cccccccc-0003-0003-0003-000000000005', 'Comedy'),
    ('cccccccc-0003-0003-0003-000000000006', 'Animation'),
    ('cccccccc-0003-0003-0003-000000000007', 'Horror');

-- -------------------------
-- ACTORS
-- -------------------------
INSERT INTO actor (id, name, url_image) VALUES
    ('dddddddd-0004-0004-0004-000000000001', 'Leonardo DiCaprio',  'https://images.example.com/actors/leo.jpg'),
    ('dddddddd-0004-0004-0004-000000000002', 'Joseph Gordon-Levitt','https://images.example.com/actors/jgl.jpg'),
    ('dddddddd-0004-0004-0004-000000000003', 'Elliot Page',         'https://images.example.com/actors/ep.jpg'),
    ('dddddddd-0004-0004-0004-000000000004', 'Tom Hardy',           'https://images.example.com/actors/th.jpg'),
    ('dddddddd-0004-0004-0004-000000000005', 'Keanu Reeves',        'https://images.example.com/actors/kr.jpg'),
    ('dddddddd-0004-0004-0004-000000000006', 'Carrie-Anne Moss',    'https://images.example.com/actors/cam.jpg');

-- -------------------------
-- PEOPLE (directores, guionistas, productores)
-- -------------------------
INSERT INTO people (id, name) VALUES
    ('eeeeeeee-0005-0005-0005-000000000001', 'Christopher Nolan'),
    ('eeeeeeee-0005-0005-0005-000000000002', 'Emma Thomas'),
    ('eeeeeeee-0005-0005-0005-000000000003', 'Lana Wachowski'),
    ('eeeeeeee-0005-0005-0005-000000000004', 'Lilly Wachowski');

-- -------------------------
-- MOVIES
-- -------------------------
INSERT INTO movie (id, title, synopsis, duration, trailer_link, original_language, release_date, created_at, updated_at) VALUES
    (
        'ffffffff-0006-0006-0006-000000000001',
        'Inception',
        'Un ladrón que roba secretos corporativos mediante el uso de tecnología de sueños compartidos.',
        148,
        'https://www.youtube.com/watch?v=YoHD9XEInc0',
        'English',
        '2027-07-16',
        NOW(), NOW()
    ),
    (
        'ffffffff-0006-0006-0006-000000000002',
        'The Matrix',
        'Un programador descubre que la realidad tal como la conoce es una simulación creada por máquinas.',
        136,
        'https://www.youtube.com/watch?v=vKQi3bBA1y8',
        'English',
        '2027-03-31',
        NOW(), NOW()
    );

-- -------------------------
-- MOVIE_COUNTRY_INFO
-- Inception: disponible en USA (PG-13) y Mexico (B)
-- The Matrix: disponible en USA (R) y Spain (16)
-- -------------------------
INSERT INTO movie_country_info (id, classification_id, movie_id, is_active) VALUES
    ('11111111-0007-0007-0007-000000000001', 'bbbbbbbb-0002-0002-0002-000000000003', 'ffffffff-0006-0006-0006-000000000001', TRUE),  -- Inception - PG-13 USA
    ('11111111-0007-0007-0007-000000000002', 'bbbbbbbb-0002-0002-0002-000000000007', 'ffffffff-0006-0006-0006-000000000001', TRUE),  -- Inception - B Mexico
    ('11111111-0007-0007-0007-000000000003', 'bbbbbbbb-0002-0002-0002-000000000004', 'ffffffff-0006-0006-0006-000000000002', TRUE),  -- The Matrix - R USA
    ('11111111-0007-0007-0007-000000000004', 'bbbbbbbb-0002-0002-0002-000000000012', 'ffffffff-0006-0006-0006-000000000002', TRUE);  -- The Matrix - 16 Spain

-- -------------------------
-- POSTERS
-- -------------------------
INSERT INTO poster (id, movie_id, url_image, is_main) VALUES
    ('22222222-0008-0008-0008-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'https://images.example.com/inception-main.jpg',    TRUE),
    ('22222222-0008-0008-0008-000000000002', 'ffffffff-0006-0006-0006-000000000001', 'https://images.example.com/inception-alt.jpg',     FALSE),
    ('22222222-0008-0008-0008-000000000003', 'ffffffff-0006-0006-0006-000000000002', 'https://images.example.com/matrix-main.jpg',       TRUE),
    ('22222222-0008-0008-0008-000000000004', 'ffffffff-0006-0006-0006-000000000002', 'https://images.example.com/matrix-alt.jpg',        FALSE);

-- -------------------------
-- MOVIE_CATEGORIES
-- -------------------------
INSERT INTO movie_categories (id, movie_id, category_id) VALUES
    ('33333333-0009-0009-0009-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'cccccccc-0003-0003-0003-000000000003'), -- Inception - Sci-Fi
    ('33333333-0009-0009-0009-000000000002', 'ffffffff-0006-0006-0006-000000000001', 'cccccccc-0003-0003-0003-000000000004'), -- Inception - Thriller
    ('33333333-0009-0009-0009-000000000003', 'ffffffff-0006-0006-0006-000000000002', 'cccccccc-0003-0003-0003-000000000001'), -- The Matrix - Action
    ('33333333-0009-0009-0009-000000000004', 'ffffffff-0006-0006-0006-000000000002', 'cccccccc-0003-0003-0003-000000000003'); -- The Matrix - Sci-Fi

-- -------------------------
-- CAST_MOVIE
-- -------------------------
INSERT INTO cast_movie (id, movie_id, actor_id, character_name) VALUES
    ('44444444-0010-0010-0010-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000001', 'Dom Cobb'),
    ('44444444-0010-0010-0010-000000000002', 'ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000002', 'Arthur'),
    ('44444444-0010-0010-0010-000000000003', 'ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000003', 'Ariadne'),
    ('44444444-0010-0010-0010-000000000004', 'ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000004', 'Eames'),
    ('44444444-0010-0010-0010-000000000005', 'ffffffff-0006-0006-0006-000000000002', 'dddddddd-0004-0004-0004-000000000005', 'Neo'),
    ('44444444-0010-0010-0010-000000000006', 'ffffffff-0006-0006-0006-000000000002', 'dddddddd-0004-0004-0004-000000000006', 'Trinity');

-- -------------------------
-- MOVIE_PEOPLE
-- -------------------------
INSERT INTO movie_people (id, movie_id, people_id, rol) VALUES
    ('55555555-0011-0011-0011-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'eeeeeeee-0005-0005-0005-000000000001', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000002', 'ffffffff-0006-0006-0006-000000000001', 'eeeeeeee-0005-0005-0005-000000000001', 'WRITER'),
    ('55555555-0011-0011-0011-000000000003', 'ffffffff-0006-0006-0006-000000000001', 'eeeeeeee-0005-0005-0005-000000000002', 'PRODUCER'),
    ('55555555-0011-0011-0011-000000000004', 'ffffffff-0006-0006-0006-000000000002', 'eeeeeeee-0005-0005-0005-000000000003', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000005', 'ffffffff-0006-0006-0006-000000000002', 'eeeeeeee-0005-0005-0005-000000000004', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000006', 'ffffffff-0006-0006-0006-000000000002', 'eeeeeeee-0005-0005-0005-000000000003', 'WRITER'),
    ('55555555-0011-0011-0011-000000000007', 'ffffffff-0006-0006-0006-000000000002', 'eeeeeeee-0005-0005-0005-000000000004', 'WRITER');
