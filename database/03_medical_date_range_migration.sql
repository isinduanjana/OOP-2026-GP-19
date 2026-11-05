-- Run this ONCE on an existing database.
-- Keeps medical_date for compatibility with older screens/queries.
ALTER TABLE medicals
    ADD COLUMN from_date DATE NULL AFTER medical_date,
    ADD COLUMN to_date DATE NULL AFTER from_date;

UPDATE medicals
SET from_date = medical_date,
    to_date = medical_date
WHERE from_date IS NULL OR to_date IS NULL;

ALTER TABLE medicals
    MODIFY from_date DATE NOT NULL,
    MODIFY to_date DATE NOT NULL;

-- created_at already exists in the original schema and is the submission timestamp.
