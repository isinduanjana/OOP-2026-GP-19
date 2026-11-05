-- =====================================================================
--  Faculty of Technology Academic Management System
--  Database : faculty_academic_system   (MySQL 8.x)
--  File     : 01_schema.sql   [COMMON PART - shared by M1, M2, M3, M4]
--
--  Follows the group ER diagram. Small additions (marked "ADDED") were
--  needed to satisfy the ICT2132 handout:
--    * courses.level / courses.semester  -> needed to calculate SGPA per semester
--    * undergraduates.level              -> which level the student is in
--    * attendance_sessions.hours         -> 1 session = 2 hours (handout rule)
--    * grades.academic_year / remarks    -> published result history
--    * course_materials.covers_from / covers_to -> date range a material covers
--  Run this file first, then 02_seed_data.sql
--  (The application also runs both automatically on first start.)
-- =====================================================================

DROP DATABASE IF EXISTS faculty_academic_system;
CREATE DATABASE faculty_academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE faculty_academic_system;

-- ---------------------------------------------------------------- master
CREATE TABLE departments (
    department_id   INT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(10)  NOT NULL UNIQUE,
    department_name VARCHAR(100) NOT NULL
);

-- ---------------------------------------------------------------- users (login accounts)
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,                 -- SHA-256 hash (see PasswordUtil.java)
    role       ENUM('ADMIN','LECTURER','TO','UNDERGRADUATE') NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 1 user : 1 profile
CREATE TABLE user_profiles (
    profile_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(15),
    address         TEXT,
    profile_picture VARCHAR(255),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- role tables (1 user : 1 role row)
CREATE TABLE admins (
    admin_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL UNIQUE,
    employee_no   VARCHAR(20) NOT NULL UNIQUE,
    department_id INT NULL,
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_dept FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
);

CREATE TABLE lecturers (
    lecturer_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL UNIQUE,
    employee_no   VARCHAR(20) NOT NULL UNIQUE,
    department_id INT NULL,
    CONSTRAINT fk_lecturer_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_lecturer_dept FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
);

CREATE TABLE tech_officers (
    to_id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL UNIQUE,
    employee_no   VARCHAR(20) NOT NULL UNIQUE,
    department_id INT NULL,
    CONSTRAINT fk_to_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_to_dept FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
);

CREATE TABLE undergraduates (
    student_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL UNIQUE,
    tg_no         VARCHAR(20) NOT NULL UNIQUE,
    batch         VARCHAR(10) NOT NULL,
    level         INT NOT NULL DEFAULT 1,              -- ADDED
    semester      INT NOT NULL DEFAULT 1,
    student_type  ENUM('NORMAL','REPEAT','BATCH_MISSED') NOT NULL DEFAULT 'NORMAL',
    department_id INT NULL,
    CONSTRAINT fk_ug_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ug_dept FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------- academic
CREATE TABLE courses (
    course_id       INT AUTO_INCREMENT PRIMARY KEY,
    course_code     VARCHAR(20)  NOT NULL UNIQUE,
    course_name     VARCHAR(100) NOT NULL,
    credits         INT NOT NULL,
    theory_hours    INT NOT NULL DEFAULT 0,   -- hours per THEORY session (0 = no theory part)
    practical_hours INT NOT NULL DEFAULT 0,   -- hours per PRACTICAL session (0 = no practical part)
    description     TEXT,
    department_id   INT NULL,
    level           INT NOT NULL DEFAULT 1,   -- ADDED
    semester        INT NOT NULL DEFAULT 1,   -- ADDED
    CONSTRAINT fk_course_dept FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
);

CREATE TABLE course_lecturers (
    course_lecturer_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id   INT NOT NULL,
    lecturer_id INT NOT NULL,
    UNIQUE KEY uq_course_lecturer (course_id, lecturer_id),
    CONSTRAINT fk_cl_course   FOREIGN KEY (course_id)   REFERENCES courses(course_id)     ON DELETE CASCADE,
    CONSTRAINT fk_cl_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(lecturer_id) ON DELETE CASCADE
);

CREATE TABLE course_materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id   INT NOT NULL,
    lecturer_id INT NULL,
    title       VARCHAR(100) NOT NULL,
    file_path   VARCHAR(255),
    covers_from DATE NULL,                         -- ADDED: first date the material covers
    covers_to   DATE NULL,                         -- ADDED: last date the material covers
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cm_dates CHECK (covers_to IS NULL OR covers_from IS NULL OR covers_to >= covers_from),
    CONSTRAINT fk_cm_course  FOREIGN KEY (course_id)   REFERENCES courses(course_id)     ON DELETE CASCADE,
    CONSTRAINT fk_cm_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(lecturer_id) ON DELETE SET NULL
);

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    semester      INT NOT NULL,
    status        ENUM('ENROLLED','WITHDRAWN','COMPLETED') NOT NULL DEFAULT 'ENROLLED',
    UNIQUE KEY uq_enrollment (student_id, course_id, academic_year),
    CONSTRAINT fk_en_student FOREIGN KEY (student_id) REFERENCES undergraduates(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_en_course  FOREIGN KEY (course_id)  REFERENCES courses(course_id)         ON DELETE CASCADE
);

-- ---------------------------------------------------------------- assessments & marks (all marks out of 100)
CREATE TABLE assessments (
    assessment_id   INT AUTO_INCREMENT PRIMARY KEY,
    course_id       INT NOT NULL,
    assessment_name VARCHAR(50) NOT NULL,
    assessment_type ENUM('CA','MID','FINAL','ASSIGNMENT') NOT NULL,
    max_marks       INT NOT NULL DEFAULT 100,
    percentage      DECIMAL(5,2) NOT NULL,       -- weight of this assessment in the final 100
    CONSTRAINT fk_as_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

CREATE TABLE marks (
    mark_id       INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id INT NOT NULL,
    student_id    INT NOT NULL,
    marks         DECIMAL(5,2) NOT NULL,
    entered_by    INT NULL,                       -- users.user_id of the lecturer
    entered_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_mark (assessment_id, student_id),
    CONSTRAINT fk_mk_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_mk_student    FOREIGN KEY (student_id)    REFERENCES undergraduates(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_mk_user       FOREIGN KEY (entered_by)    REFERENCES users(user_id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------- attendance & medicals
CREATE TABLE attendance_sessions (
    session_id     INT AUTO_INCREMENT PRIMARY KEY,
    course_id      INT NOT NULL,
    session_date   DATE NOT NULL,
    component      ENUM('THEORY','PRACTICAL') NOT NULL,
    session_number INT NOT NULL,
    total_sessions INT NOT NULL DEFAULT 15,
    hours          INT NOT NULL DEFAULT 2,         -- ADDED: 1 session = 2 hours
    UNIQUE KEY uq_session (course_id, component, session_number),
    CONSTRAINT fk_ses_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

CREATE TABLE medicals (
    medical_id    INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NULL,                        -- NULL = covers every course on that date
    medical_date  DATE NOT NULL,
    reason        TEXT,
    document_path VARCHAR(255),
    status        ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    approved_by   INT NULL,                        -- users.user_id
    approved_at   DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_med_student FOREIGN KEY (student_id)  REFERENCES undergraduates(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_med_course  FOREIGN KEY (course_id)   REFERENCES courses(course_id) ON DELETE SET NULL,
    CONSTRAINT fk_med_user    FOREIGN KEY (approved_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id    INT NOT NULL,
    student_id    INT NOT NULL,
    status        ENUM('PRESENT','ABSENT') NOT NULL,
    medical_id    INT NULL,                        -- set when an absence is covered by a medical
    UNIQUE KEY uq_attendance (session_id, student_id),
    CONSTRAINT fk_att_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(session_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES undergraduates(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_medical FOREIGN KEY (medical_id) REFERENCES medicals(medical_id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------- results
CREATE TABLE grades (
    grade_id      INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    academic_year VARCHAR(9),                      -- ADDED
    total_marks   DECIMAL(5,2),
    grade         VARCHAR(2) NOT NULL,
    grade_point   DECIMAL(3,2) NOT NULL,
    sgpa          DECIMAL(3,2) NULL,
    cgpa          DECIMAL(3,2) NULL,
    remarks       VARCHAR(50) NULL,                -- ADDED
    UNIQUE KEY uq_grade (student_id, course_id),
    CONSTRAINT fk_gr_student FOREIGN KEY (student_id) REFERENCES undergraduates(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_gr_course  FOREIGN KEY (course_id)  REFERENCES courses(course_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- other
CREATE TABLE notices (
    notice_id   INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    created_by  INT NULL,
    target_role ENUM('ALL','LECTURER','TO','UNDERGRADUATE') NOT NULL DEFAULT 'ALL',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_notice_user FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE timetables (
    timetable_id  INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NOT NULL,
    course_id     INT NOT NULL,
    day           VARCHAR(10) NOT NULL,            -- MONDAY..FRIDAY
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    room          VARCHAR(20),
    component     ENUM('THEORY','PRACTICAL') NOT NULL,
    CONSTRAINT fk_tt_dept   FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_course FOREIGN KEY (course_id)     REFERENCES courses(course_id) ON DELETE CASCADE
);
