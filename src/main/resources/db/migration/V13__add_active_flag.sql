-- Add is_active flag to actor, people, and classification tables
ALTER TABLE actor ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE people ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE classification ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
