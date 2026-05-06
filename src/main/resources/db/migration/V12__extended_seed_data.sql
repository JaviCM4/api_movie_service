-- =============================================================
-- V12 - Extended seed data para pruebas en Postman
-- Agrega: más países, clasificaciones, categorías, actores,
--         personas, películas y todas sus relaciones
-- =============================================================

-- -------------------------
-- COUNTRIES (nuevos)
-- -------------------------
INSERT INTO country (id, name) VALUES
    ('aaaaaaaa-0001-0001-0001-000000000004', 'United Kingdom'),
    ('aaaaaaaa-0001-0001-0001-000000000005', 'France'),
    ('aaaaaaaa-0001-0001-0001-000000000006', 'Japan'),
    ('aaaaaaaa-0001-0001-0001-000000000007', 'Argentina'),
    ('aaaaaaaa-0001-0001-0001-000000000008', 'Germany');

-- -------------------------
-- CLASSIFICATIONS (nuevas por país)
-- -------------------------
INSERT INTO classification (id, country_id, name, age_limit) VALUES
    -- United Kingdom
    ('bbbbbbbb-0002-0002-0002-000000000013', 'aaaaaaaa-0001-0001-0001-000000000004', 'U',    0),
    ('bbbbbbbb-0002-0002-0002-000000000014', 'aaaaaaaa-0001-0001-0001-000000000004', 'PG',   8),
    ('bbbbbbbb-0002-0002-0002-000000000015', 'aaaaaaaa-0001-0001-0001-000000000004', '12A', 12),
    ('bbbbbbbb-0002-0002-0002-000000000016', 'aaaaaaaa-0001-0001-0001-000000000004', '15',  15),
    ('bbbbbbbb-0002-0002-0002-000000000017', 'aaaaaaaa-0001-0001-0001-000000000004', '18',  18),
    -- France
    ('bbbbbbbb-0002-0002-0002-000000000018', 'aaaaaaaa-0001-0001-0001-000000000005', 'Tout public',   0),
    ('bbbbbbbb-0002-0002-0002-000000000019', 'aaaaaaaa-0001-0001-0001-000000000005', '-10',           10),
    ('bbbbbbbb-0002-0002-0002-000000000020', 'aaaaaaaa-0001-0001-0001-000000000005', '-12',           12),
    ('bbbbbbbb-0002-0002-0002-000000000021', 'aaaaaaaa-0001-0001-0001-000000000005', '-16',           16),
    ('bbbbbbbb-0002-0002-0002-000000000022', 'aaaaaaaa-0001-0001-0001-000000000005', '-18',           18),
    -- Japan
    ('bbbbbbbb-0002-0002-0002-000000000023', 'aaaaaaaa-0001-0001-0001-000000000006', 'G',    0),
    ('bbbbbbbb-0002-0002-0002-000000000024', 'aaaaaaaa-0001-0001-0001-000000000006', 'PG12', 12),
    ('bbbbbbbb-0002-0002-0002-000000000025', 'aaaaaaaa-0001-0001-0001-000000000006', 'R15+', 15),
    ('bbbbbbbb-0002-0002-0002-000000000026', 'aaaaaaaa-0001-0001-0001-000000000006', 'R18+', 18),
    -- Argentina
    ('bbbbbbbb-0002-0002-0002-000000000027', 'aaaaaaaa-0001-0001-0001-000000000007', 'ATP',  0),
    ('bbbbbbbb-0002-0002-0002-000000000028', 'aaaaaaaa-0001-0001-0001-000000000007', '+13', 13),
    ('bbbbbbbb-0002-0002-0002-000000000029', 'aaaaaaaa-0001-0001-0001-000000000007', '+16', 16),
    ('bbbbbbbb-0002-0002-0002-000000000030', 'aaaaaaaa-0001-0001-0001-000000000007', '+18', 18),
    -- Germany
    ('bbbbbbbb-0002-0002-0002-000000000031', 'aaaaaaaa-0001-0001-0001-000000000008', 'FSK 0',  0),
    ('bbbbbbbb-0002-0002-0002-000000000032', 'aaaaaaaa-0001-0001-0001-000000000008', 'FSK 6',  6),
    ('bbbbbbbb-0002-0002-0002-000000000033', 'aaaaaaaa-0001-0001-0001-000000000008', 'FSK 12', 12),
    ('bbbbbbbb-0002-0002-0002-000000000034', 'aaaaaaaa-0001-0001-0001-000000000008', 'FSK 16', 16),
    ('bbbbbbbb-0002-0002-0002-000000000035', 'aaaaaaaa-0001-0001-0001-000000000008', 'FSK 18', 18);

-- -------------------------
-- CATEGORIES (nuevas)
-- -------------------------
INSERT INTO category (id, name, is_active) VALUES
    ('cccccccc-0003-0003-0003-000000000008', 'Romance',    TRUE),
    ('cccccccc-0003-0003-0003-000000000009', 'Adventure',  TRUE),
    ('cccccccc-0003-0003-0003-000000000010', 'Fantasy',    TRUE),
    ('cccccccc-0003-0003-0003-000000000011', 'Crime',      TRUE),
    ('cccccccc-0003-0003-0003-000000000012', 'Biography',  FALSE);

-- -------------------------
-- ACTORS (nuevos)
-- -------------------------
INSERT INTO actor (id, name, url_image) VALUES
    ('dddddddd-0004-0004-0004-000000000007', 'Scarlett Johansson',  'https://images.example.com/actors/sj.jpg'),
    ('dddddddd-0004-0004-0004-000000000008', 'Robert Downey Jr.',   'https://images.example.com/actors/rdj.jpg'),
    ('dddddddd-0004-0004-0004-000000000009', 'Christian Bale',      'https://images.example.com/actors/cb.jpg'),
    ('dddddddd-0004-0004-0004-000000000010', 'Heath Ledger',        'https://images.example.com/actors/hl.jpg'),
    ('dddddddd-0004-0004-0004-000000000011', 'Cate Blanchett',      'https://images.example.com/actors/cbl.jpg'),
    ('dddddddd-0004-0004-0004-000000000012', 'Brad Pitt',           'https://images.example.com/actors/bp.jpg'),
    ('dddddddd-0004-0004-0004-000000000013', 'Morgan Freeman',      'https://images.example.com/actors/mf.jpg'),
    ('dddddddd-0004-0004-0004-000000000014', 'Natalie Portman',     'https://images.example.com/actors/np.jpg'),
    ('dddddddd-0004-0004-0004-000000000015', 'Tom Hanks',           'https://images.example.com/actors/toh.jpg'),
    ('dddddddd-0004-0004-0004-000000000016', 'Anne Hathaway',       'https://images.example.com/actors/ah.jpg');

-- -------------------------
-- PEOPLE (nuevos)
-- -------------------------
INSERT INTO people (id, name) VALUES
    ('eeeeeeee-0005-0005-0005-000000000005', 'David Fincher'),
    ('eeeeeeee-0005-0005-0005-000000000006', 'James Cameron'),
    ('eeeeeeee-0005-0005-0005-000000000007', 'Steven Spielberg'),
    ('eeeeeeee-0005-0005-0005-000000000008', 'Ridley Scott'),
    ('eeeeeeee-0005-0005-0005-000000000009', 'Jonathan Nolan'),
    ('eeeeeeee-0005-0005-0005-000000000010', 'Aaron Sorkin'),
    ('eeeeeeee-0005-0005-0005-000000000011', 'Kathleen Kennedy');

-- -------------------------
-- MOVIES (nuevas)
-- -------------------------
INSERT INTO movie (id, title, synopsis, duration, trailer_link, original_language, release_date, allow_comments, allow_ratings, created_at, updated_at) VALUES
    (
        'ffffffff-0006-0006-0006-000000000003',
        'The Dark Knight',
        'Batman enfrenta al Joker, un criminal caótico que busca sumir Gotham en el terror.',
        152,
        'https://www.youtube.com/watch?v=EXeTwQWrcwY',
        'English',
        '2008-07-18',
        TRUE, TRUE,
        NOW(), NOW()
    ),
    (
        'ffffffff-0006-0006-0006-000000000004',
        'Interstellar',
        'Un grupo de astronautas viaja a través de un agujero de gusano en busca de un nuevo hogar para la humanidad.',
        169,
        'https://www.youtube.com/watch?v=zSWdZVtXT7E',
        'English',
        '2014-11-07',
        TRUE, TRUE,
        NOW(), NOW()
    ),
    (
        'ffffffff-0006-0006-0006-000000000005',
        'Se7en',
        'Dos detectives persiguen a un asesino en serie que utiliza los siete pecados capitales como motivo.',
        127,
        'https://www.youtube.com/watch?v=znmZoVkCjpI',
        'English',
        '1995-09-22',
        TRUE, TRUE,
        NOW(), NOW()
    ),
    (
        'ffffffff-0006-0006-0006-000000000006',
        'Titanic',
        'Un romance épico entre dos personas de clases sociales opuestas a bordo del famoso transatlántico.',
        194,
        'https://www.youtube.com/watch?v=kVrqfYjkTdQ',
        'English',
        '1997-12-19',
        TRUE, TRUE,
        NOW(), NOW()
    ),
    (
        'ffffffff-0006-0006-0006-000000000007',
        'Forrest Gump',
        'La vida extraordinaria de un hombre de Alabama con un coeficiente intelectual bajo que participa en eventos históricos de los EE.UU.',
        142,
        'https://www.youtube.com/watch?v=bLvqoHBptjg',
        'English',
        '1994-07-06',
        TRUE, TRUE,
        NOW(), NOW()
    ),
    (
        'ffffffff-0006-0006-0006-000000000008',
        'Avengers: Endgame',
        'Los Vengadores sobrevivientes ensamblan una vez más para revertir las acciones de Thanos y restaurar el equilibrio del universo.',
        181,
        'https://www.youtube.com/watch?v=TcMBFSGVi1c',
        'English',
        '2019-04-26',
        FALSE, TRUE,
        NOW(), NOW()
    );

-- -------------------------
-- MOVIE_COUNTRY_INFO
-- Una clasificación por país por película
-- -------------------------
INSERT INTO movie_country_info (id, classification_id, movie_id, is_active) VALUES
    -- The Dark Knight
    ('11111111-0007-0007-0007-000000000005', 'bbbbbbbb-0002-0002-0002-000000000004', 'ffffffff-0006-0006-0006-000000000003', TRUE),   -- R USA
    ('11111111-0007-0007-0007-000000000006', 'bbbbbbbb-0002-0002-0002-000000000016', 'ffffffff-0006-0006-0006-000000000003', TRUE),   -- 15 UK
    ('11111111-0007-0007-0007-000000000007', 'bbbbbbbb-0002-0002-0002-000000000008', 'ffffffff-0006-0006-0006-000000000003', FALSE),  -- C Mexico (inactiva)
    -- Interstellar
    ('11111111-0007-0007-0007-000000000008', 'bbbbbbbb-0002-0002-0002-000000000003', 'ffffffff-0006-0006-0006-000000000004', TRUE),   -- PG-13 USA
    ('11111111-0007-0007-0007-000000000009', 'bbbbbbbb-0002-0002-0002-000000000014', 'ffffffff-0006-0006-0006-000000000004', TRUE),   -- PG UK
    ('11111111-0007-0007-0007-000000000010', 'bbbbbbbb-0002-0002-0002-000000000024', 'ffffffff-0006-0006-0006-000000000004', TRUE),   -- PG12 Japan
    -- Se7en
    ('11111111-0007-0007-0007-000000000011', 'bbbbbbbb-0002-0002-0002-000000000004', 'ffffffff-0006-0006-0006-000000000005', TRUE),   -- R USA
    ('11111111-0007-0007-0007-000000000012', 'bbbbbbbb-0002-0002-0002-000000000012', 'ffffffff-0006-0006-0006-000000000005', TRUE),   -- 16 Spain
    ('11111111-0007-0007-0007-000000000013', 'bbbbbbbb-0002-0002-0002-000000000034', 'ffffffff-0006-0006-0006-000000000005', TRUE),   -- FSK 16 Germany
    -- Titanic
    ('11111111-0007-0007-0007-000000000014', 'bbbbbbbb-0002-0002-0002-000000000003', 'ffffffff-0006-0006-0006-000000000006', TRUE),   -- PG-13 USA
    ('11111111-0007-0007-0007-000000000015', 'bbbbbbbb-0002-0002-0002-000000000021', 'ffffffff-0006-0006-0006-000000000006', TRUE),   -- -16 France
    ('11111111-0007-0007-0007-000000000016', 'bbbbbbbb-0002-0002-0002-000000000029', 'ffffffff-0006-0006-0006-000000000006', TRUE),   -- +16 Argentina
    -- Forrest Gump
    ('11111111-0007-0007-0007-000000000017', 'bbbbbbbb-0002-0002-0002-000000000002', 'ffffffff-0006-0006-0006-000000000007', TRUE),   -- PG USA
    ('11111111-0007-0007-0007-000000000018', 'bbbbbbbb-0002-0002-0002-000000000009', 'ffffffff-0006-0006-0006-000000000007', TRUE),   -- APTA Spain
    -- Avengers: Endgame
    ('11111111-0007-0007-0007-000000000019', 'bbbbbbbb-0002-0002-0002-000000000003', 'ffffffff-0006-0006-0006-000000000008', TRUE),   -- PG-13 USA
    ('11111111-0007-0007-0007-000000000020', 'bbbbbbbb-0002-0002-0002-000000000013', 'ffffffff-0006-0006-0006-000000000008', TRUE),   -- U UK
    ('11111111-0007-0007-0007-000000000021', 'bbbbbbbb-0002-0002-0002-000000000027', 'ffffffff-0006-0006-0006-000000000008', TRUE);  -- ATP Argentina

-- -------------------------
-- POSTERS
-- -------------------------
INSERT INTO poster (id, movie_id, url_image, is_main) VALUES
    ('22222222-0008-0008-0008-000000000005', 'ffffffff-0006-0006-0006-000000000003', 'https://images.example.com/dark-knight-main.jpg',    TRUE),
    ('22222222-0008-0008-0008-000000000006', 'ffffffff-0006-0006-0006-000000000003', 'https://images.example.com/dark-knight-alt.jpg',     FALSE),
    ('22222222-0008-0008-0008-000000000007', 'ffffffff-0006-0006-0006-000000000004', 'https://images.example.com/interstellar-main.jpg',   TRUE),
    ('22222222-0008-0008-0008-000000000008', 'ffffffff-0006-0006-0006-000000000004', 'https://images.example.com/interstellar-alt1.jpg',   FALSE),
    ('22222222-0008-0008-0008-000000000009', 'ffffffff-0006-0006-0006-000000000004', 'https://images.example.com/interstellar-alt2.jpg',   FALSE),
    ('22222222-0008-0008-0008-000000000010', 'ffffffff-0006-0006-0006-000000000005', 'https://images.example.com/seven-main.jpg',          TRUE),
    ('22222222-0008-0008-0008-000000000011', 'ffffffff-0006-0006-0006-000000000006', 'https://images.example.com/titanic-main.jpg',        TRUE),
    ('22222222-0008-0008-0008-000000000012', 'ffffffff-0006-0006-0006-000000000006', 'https://images.example.com/titanic-alt.jpg',         FALSE),
    ('22222222-0008-0008-0008-000000000013', 'ffffffff-0006-0006-0006-000000000007', 'https://images.example.com/forrest-main.jpg',        TRUE),
    ('22222222-0008-0008-0008-000000000014', 'ffffffff-0006-0006-0006-000000000008', 'https://images.example.com/avengers-main.jpg',       TRUE),
    ('22222222-0008-0008-0008-000000000015', 'ffffffff-0006-0006-0006-000000000008', 'https://images.example.com/avengers-alt.jpg',        FALSE);

-- -------------------------
-- MOVIE_CATEGORIES
-- -------------------------
INSERT INTO movie_categories (id, movie_id, category_id) VALUES
    -- The Dark Knight
    ('33333333-0009-0009-0009-000000000005', 'ffffffff-0006-0006-0006-000000000003', 'cccccccc-0003-0003-0003-000000000001'), -- Action
    ('33333333-0009-0009-0009-000000000006', 'ffffffff-0006-0006-0006-000000000003', 'cccccccc-0003-0003-0003-000000000004'), -- Thriller
    ('33333333-0009-0009-0009-000000000007', 'ffffffff-0006-0006-0006-000000000003', 'cccccccc-0003-0003-0003-000000000011'), -- Crime
    -- Interstellar
    ('33333333-0009-0009-0009-000000000008', 'ffffffff-0006-0006-0006-000000000004', 'cccccccc-0003-0003-0003-000000000003'), -- Sci-Fi
    ('33333333-0009-0009-0009-000000000009', 'ffffffff-0006-0006-0006-000000000004', 'cccccccc-0003-0003-0003-000000000002'), -- Drama
    ('33333333-0009-0009-0009-000000000010', 'ffffffff-0006-0006-0006-000000000004', 'cccccccc-0003-0003-0003-000000000009'), -- Adventure
    -- Se7en
    ('33333333-0009-0009-0009-000000000011', 'ffffffff-0006-0006-0006-000000000005', 'cccccccc-0003-0003-0003-000000000004'), -- Thriller
    ('33333333-0009-0009-0009-000000000012', 'ffffffff-0006-0006-0006-000000000005', 'cccccccc-0003-0003-0003-000000000011'), -- Crime
    -- Titanic
    ('33333333-0009-0009-0009-000000000013', 'ffffffff-0006-0006-0006-000000000006', 'cccccccc-0003-0003-0003-000000000002'), -- Drama
    ('33333333-0009-0009-0009-000000000014', 'ffffffff-0006-0006-0006-000000000006', 'cccccccc-0003-0003-0003-000000000008'), -- Romance
    -- Forrest Gump
    ('33333333-0009-0009-0009-000000000015', 'ffffffff-0006-0006-0006-000000000007', 'cccccccc-0003-0003-0003-000000000002'), -- Drama
    ('33333333-0009-0009-0009-000000000016', 'ffffffff-0006-0006-0006-000000000007', 'cccccccc-0003-0003-0003-000000000008'), -- Romance
    -- Avengers: Endgame
    ('33333333-0009-0009-0009-000000000017', 'ffffffff-0006-0006-0006-000000000008', 'cccccccc-0003-0003-0003-000000000001'), -- Action
    ('33333333-0009-0009-0009-000000000018', 'ffffffff-0006-0006-0006-000000000008', 'cccccccc-0003-0003-0003-000000000003'), -- Sci-Fi
    ('33333333-0009-0009-0009-000000000019', 'ffffffff-0006-0006-0006-000000000008', 'cccccccc-0003-0003-0003-000000000009'); -- Adventure

-- -------------------------
-- CAST_MOVIE
-- -------------------------
INSERT INTO cast_movie (id, movie_id, actor_id, character_name) VALUES
    -- The Dark Knight
    ('44444444-0010-0010-0010-000000000007', 'ffffffff-0006-0006-0006-000000000003', 'dddddddd-0004-0004-0004-000000000009', 'Bruce Wayne / Batman'),
    ('44444444-0010-0010-0010-000000000008', 'ffffffff-0006-0006-0006-000000000003', 'dddddddd-0004-0004-0004-000000000010', 'The Joker'),
    -- Interstellar
    ('44444444-0010-0010-0010-000000000009', 'ffffffff-0006-0006-0006-000000000004', 'dddddddd-0004-0004-0004-000000000016', 'Dr. Amelia Brand'),
    ('44444444-0010-0010-0010-000000000010', 'ffffffff-0006-0006-0006-000000000004', 'dddddddd-0004-0004-0004-000000000011', 'Dr. Murph Cooper'),
    -- Se7en
    ('44444444-0010-0010-0010-000000000011', 'ffffffff-0006-0006-0006-000000000005', 'dddddddd-0004-0004-0004-000000000012', 'Detective Mills'),
    ('44444444-0010-0010-0010-000000000012', 'ffffffff-0006-0006-0006-000000000005', 'dddddddd-0004-0004-0004-000000000013', 'Detective Somerset'),
    -- Titanic
    ('44444444-0010-0010-0010-000000000013', 'ffffffff-0006-0006-0006-000000000006', 'dddddddd-0004-0004-0004-000000000001', 'Jack Dawson'),
    -- Forrest Gump
    ('44444444-0010-0010-0010-000000000014', 'ffffffff-0006-0006-0006-000000000007', 'dddddddd-0004-0004-0004-000000000015', 'Forrest Gump'),
    -- Avengers: Endgame
    ('44444444-0010-0010-0010-000000000015', 'ffffffff-0006-0006-0006-000000000008', 'dddddddd-0004-0004-0004-000000000007', 'Natasha Romanoff'),
    ('44444444-0010-0010-0010-000000000016', 'ffffffff-0006-0006-0006-000000000008', 'dddddddd-0004-0004-0004-000000000008', 'Tony Stark / Iron Man');

-- -------------------------
-- MOVIE_PEOPLE
-- -------------------------
INSERT INTO movie_people (id, movie_id, people_id, rol) VALUES
    -- The Dark Knight
    ('55555555-0011-0011-0011-000000000008', 'ffffffff-0006-0006-0006-000000000003', 'eeeeeeee-0005-0005-0005-000000000001', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000009', 'ffffffff-0006-0006-0006-000000000003', 'eeeeeeee-0005-0005-0005-000000000009', 'WRITER'),
    ('55555555-0011-0011-0011-000000000010', 'ffffffff-0006-0006-0006-000000000003', 'eeeeeeee-0005-0005-0005-000000000002', 'PRODUCER'),
    -- Interstellar
    ('55555555-0011-0011-0011-000000000011', 'ffffffff-0006-0006-0006-000000000004', 'eeeeeeee-0005-0005-0005-000000000001', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000012', 'ffffffff-0006-0006-0006-000000000004', 'eeeeeeee-0005-0005-0005-000000000009', 'WRITER'),
    ('55555555-0011-0011-0011-000000000013', 'ffffffff-0006-0006-0006-000000000004', 'eeeeeeee-0005-0005-0005-000000000011', 'PRODUCER'),
    -- Se7en
    ('55555555-0011-0011-0011-000000000014', 'ffffffff-0006-0006-0006-000000000005', 'eeeeeeee-0005-0005-0005-000000000005', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000015', 'ffffffff-0006-0006-0006-000000000005', 'eeeeeeee-0005-0005-0005-000000000010', 'WRITER'),
    -- Titanic
    ('55555555-0011-0011-0011-000000000016', 'ffffffff-0006-0006-0006-000000000006', 'eeeeeeee-0005-0005-0005-000000000006', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000017', 'ffffffff-0006-0006-0006-000000000006', 'eeeeeeee-0005-0005-0005-000000000006', 'WRITER'),
    ('55555555-0011-0011-0011-000000000018', 'ffffffff-0006-0006-0006-000000000006', 'eeeeeeee-0005-0005-0005-000000000006', 'PRODUCER'),
    -- Forrest Gump
    ('55555555-0011-0011-0011-000000000019', 'ffffffff-0006-0006-0006-000000000007', 'eeeeeeee-0005-0005-0005-000000000007', 'DIRECTOR'),
    ('55555555-0011-0011-0011-000000000020', 'ffffffff-0006-0006-0006-000000000007', 'eeeeeeee-0005-0005-0005-000000000010', 'WRITER'),
    -- Avengers: Endgame
    ('55555555-0011-0011-0011-000000000021', 'ffffffff-0006-0006-0006-000000000008', 'eeeeeeee-0005-0005-0005-000000000011', 'PRODUCER');

-- -------------------------
-- MOVIE_COMMENT
-- user_id simulados (UUIDs fijos para pruebas — solo hex: 0-9 a-f)
-- Usuario 1: a0000000-0000-0000-0000-000000000001
-- Usuario 2: a0000000-0000-0000-0000-000000000002
-- Usuario 3: a0000000-0000-0000-0000-000000000003
-- Usuario 4: a0000000-0000-0000-0000-000000000004
-- Usuario 5: a0000000-0000-0000-0000-000000000005
-- -------------------------
INSERT INTO movie_comment (id, movie_id, user_id, content, created_at, updated_at) VALUES
    -- Inception
    ('66666666-0012-0012-0012-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000001', 'Una obra maestra del cine moderno. La trama te mantiene al filo del asiento.', NOW() - INTERVAL '10 days', NULL),
    ('66666666-0012-0012-0012-000000000002', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000002', 'Increible pelicula, aunque la primera vez no entendi el final.', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),
    ('66666666-0012-0012-0012-000000000003', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000003', 'Nolan en su mejor momento. Los efectos visuales son alucinantes.', NOW() - INTERVAL '2 days', NULL),
    -- The Matrix
    ('66666666-0012-0012-0012-000000000004', 'ffffffff-0006-0006-0006-000000000002', 'a0000000-0000-0000-0000-000000000001', 'Revoluciono la ciencia ficcion. Keanu Reeves nacio para este papel.', NOW() - INTERVAL '8 days', NULL),
    ('66666666-0012-0012-0012-000000000005', 'ffffffff-0006-0006-0006-000000000002', 'a0000000-0000-0000-0000-000000000004', 'Las escenas de accion con bullet time siguen siendo epicas.', NOW() - INTERVAL '3 days', NULL),
    -- The Dark Knight
    ('66666666-0012-0012-0012-000000000006', 'ffffffff-0006-0006-0006-000000000003', 'a0000000-0000-0000-0000-000000000002', 'Heath Ledger como el Joker es la actuacion del siglo.', NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days'),
    ('66666666-0012-0012-0012-000000000007', 'ffffffff-0006-0006-0006-000000000003', 'a0000000-0000-0000-0000-000000000003', 'La mejor pelicula de superheroes jamas hecha. Sin discusion.', NOW() - INTERVAL '7 days', NULL),
    -- Interstellar
    ('66666666-0012-0012-0012-000000000008', 'ffffffff-0006-0006-0006-000000000004', 'a0000000-0000-0000-0000-000000000001', 'La escena del agujero negro me dejo sin palabras. Hans Zimmer epico.', NOW() - INTERVAL '20 days', NULL),
    ('66666666-0012-0012-0012-000000000009', 'ffffffff-0006-0006-0006-000000000004', 'a0000000-0000-0000-0000-000000000005', 'Cientificamente precisa y emocionalmente devastadora.', NOW() - INTERVAL '1 day', NULL),
    -- Se7en
    ('66666666-0012-0012-0012-000000000010', 'ffffffff-0006-0006-0006-000000000005', 'a0000000-0000-0000-0000-000000000004', 'El final mas perturbador que he visto en mi vida.', NOW() - INTERVAL '30 days', NULL),
    -- Titanic
    ('66666666-0012-0012-0012-000000000011', 'ffffffff-0006-0006-0006-000000000006', 'a0000000-0000-0000-0000-000000000005', 'Clasico eterno. La historia de amor mas tragica del cine.', NOW() - INTERVAL '12 days', NULL),
    ('66666666-0012-0012-0012-000000000012', 'ffffffff-0006-0006-0006-000000000006', 'a0000000-0000-0000-0000-000000000003', 'Si habia espacio en la tabla para los dos.', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),
    -- Forrest Gump
    ('66666666-0012-0012-0012-000000000013', 'ffffffff-0006-0006-0006-000000000007', 'a0000000-0000-0000-0000-000000000002', 'Tom Hanks deberia ganar todos los Oscars del mundo por este papel.', NOW() - INTERVAL '25 days', NULL),
    -- Avengers: Endgame (comentarios desactivados, pero los datos historicos existen)
    ('66666666-0012-0012-0012-000000000014', 'ffffffff-0006-0006-0006-000000000008', 'a0000000-0000-0000-0000-000000000001', 'El cierre perfecto para 11 anos de Marvel. Llore tres veces.', NOW() - INTERVAL '40 days', NULL);

-- -------------------------
-- MOVIE_RATING
-- score de 1 a 5
-- -------------------------
INSERT INTO movie_rating (id, movie_id, user_id, score, created_at) VALUES
    -- Inception
    ('77777777-0013-0013-0013-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000001', 5, NOW() - INTERVAL '10 days'),
    ('77777777-0013-0013-0013-000000000002', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000002', 5, NOW() - INTERVAL '5 days'),
    ('77777777-0013-0013-0013-000000000003', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000003', 4, NOW() - INTERVAL '2 days'),
    ('77777777-0013-0013-0013-000000000004', 'ffffffff-0006-0006-0006-000000000001', 'a0000000-0000-0000-0000-000000000004', 5, NOW() - INTERVAL '1 day'),
    -- The Matrix
    ('77777777-0013-0013-0013-000000000005', 'ffffffff-0006-0006-0006-000000000002', 'a0000000-0000-0000-0000-000000000001', 5, NOW() - INTERVAL '8 days'),
    ('77777777-0013-0013-0013-000000000006', 'ffffffff-0006-0006-0006-000000000002', 'a0000000-0000-0000-0000-000000000002', 4, NOW() - INTERVAL '3 days'),
    ('77777777-0013-0013-0013-000000000007', 'ffffffff-0006-0006-0006-000000000002', 'a0000000-0000-0000-0000-000000000005', 5, NOW() - INTERVAL '1 day'),
    -- The Dark Knight
    ('77777777-0013-0013-0013-000000000008', 'ffffffff-0006-0006-0006-000000000003', 'a0000000-0000-0000-0000-000000000001', 5, NOW() - INTERVAL '15 days'),
    ('77777777-0013-0013-0013-000000000009', 'ffffffff-0006-0006-0006-000000000003', 'a0000000-0000-0000-0000-000000000003', 5, NOW() - INTERVAL '7 days'),
    ('77777777-0013-0013-0013-000000000010', 'ffffffff-0006-0006-0006-000000000003', 'a0000000-0000-0000-0000-000000000004', 4, NOW() - INTERVAL '2 days'),
    -- Interstellar
    ('77777777-0013-0013-0013-000000000011', 'ffffffff-0006-0006-0006-000000000004', 'a0000000-0000-0000-0000-000000000001', 5, NOW() - INTERVAL '20 days'),
    ('77777777-0013-0013-0013-000000000012', 'ffffffff-0006-0006-0006-000000000004', 'a0000000-0000-0000-0000-000000000002', 4, NOW() - INTERVAL '10 days'),
    ('77777777-0013-0013-0013-000000000013', 'ffffffff-0006-0006-0006-000000000004', 'a0000000-0000-0000-0000-000000000005', 5, NOW() - INTERVAL '1 day'),
    -- Se7en
    ('77777777-0013-0013-0013-000000000014', 'ffffffff-0006-0006-0006-000000000005', 'a0000000-0000-0000-0000-000000000002', 5, NOW() - INTERVAL '30 days'),
    ('77777777-0013-0013-0013-000000000015', 'ffffffff-0006-0006-0006-000000000005', 'a0000000-0000-0000-0000-000000000004', 4, NOW() - INTERVAL '5 days'),
    -- Titanic
    ('77777777-0013-0013-0013-000000000016', 'ffffffff-0006-0006-0006-000000000006', 'a0000000-0000-0000-0000-000000000003', 5, NOW() - INTERVAL '12 days'),
    ('77777777-0013-0013-0013-000000000017', 'ffffffff-0006-0006-0006-000000000006', 'a0000000-0000-0000-0000-000000000005', 3, NOW() - INTERVAL '6 days'),
    -- Forrest Gump
    ('77777777-0013-0013-0013-000000000018', 'ffffffff-0006-0006-0006-000000000007', 'a0000000-0000-0000-0000-000000000001', 5, NOW() - INTERVAL '25 days'),
    ('77777777-0013-0013-0013-000000000019', 'ffffffff-0006-0006-0006-000000000007', 'a0000000-0000-0000-0000-000000000003', 5, NOW() - INTERVAL '8 days'),
    ('77777777-0013-0013-0013-000000000020', 'ffffffff-0006-0006-0006-000000000007', 'a0000000-0000-0000-0000-000000000004', 4, NOW() - INTERVAL '3 days'),
    -- Avengers: Endgame
    ('77777777-0013-0013-0013-000000000021', 'ffffffff-0006-0006-0006-000000000008', 'a0000000-0000-0000-0000-000000000002', 4, NOW() - INTERVAL '40 days'),
    ('77777777-0013-0013-0013-000000000022', 'ffffffff-0006-0006-0006-000000000008', 'a0000000-0000-0000-0000-000000000005', 5, NOW() - INTERVAL '20 days');
