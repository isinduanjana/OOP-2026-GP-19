-- =====================================================================
--  UPGRADE SCRIPT  [COMMON PART]  - course material date range
--
--  Adds "covers_from" / "covers_to" to course_materials so a lecturer can
--  say which period a material covers (e.g. 2026-07-06 to 2026-07-10).
--
--  You normally do NOT need to run this: the program adds the two columns
--  automatically on start-up (DatabaseInitializer.upgradeSchema()).
--  Run it by hand only if you want to update the database yourself
--  (MySQL Workbench / phpMyAdmin). Existing data is kept.
--  Run it ONCE (running it again gives "Duplicate column name").
-- =====================================================================
USE faculty_academic_system;

ALTER TABLE course_materials
    ADD COLUMN covers_from DATE NULL AFTER file_path,
    ADD COLUMN covers_to   DATE NULL AFTER covers_from;
