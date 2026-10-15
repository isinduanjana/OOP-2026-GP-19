CREATE DATABASE IF NOT EXISTS faculty_academic_system
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE faculty_academic_system;

-- =====================================================
-- 1. DEPARTMENTS
-- =====================================================

CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;


-- =====================================================
-- 2. USERS
-- =====================================================

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','LECTURER','TO','UNDERGRADUATE') NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- =====================================================
-- 3. USER PROFILES
-- =====================================================

CREATE TABLE user_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    address TEXT,
    profile_picture VARCHAR(255),

    CONSTRAINT fk_profile_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 4. ADMINS
-- =====================================================

CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employee_no VARCHAR(20) UNIQUE,
    department_id INT,

    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_admin_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 5. LECTURERS
-- =====================================================

CREATE TABLE lecturers (
    lecturer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employee_no VARCHAR(20) UNIQUE,
    department_id INT,

    CONSTRAINT fk_lecturer_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_lecturer_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 6. TECHNICAL OFFICERS
-- =====================================================

CREATE TABLE tech_officers (
    to_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employee_no VARCHAR(20) UNIQUE,
    department_id INT,

    CONSTRAINT fk_to_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_to_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 7. UNDERGRADUATES
-- =====================================================

CREATE TABLE undergraduates (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    tg_no VARCHAR(20) NOT NULL UNIQUE,
    batch VARCHAR(10),
    semester INT,
    student_type ENUM('NORMAL','REPEAT','BATCH_MISSED') DEFAULT 'NORMAL',
    department_id INT,

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_student_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 8. COURSES
-- =====================================================

CREATE TABLE courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    theory_hours INT DEFAULT 0,
    practical_hours INT DEFAULT 0,
    description TEXT,
    department_id INT,

    CONSTRAINT fk_course_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 9. COURSE LECTURERS
-- =====================================================

CREATE TABLE course_lecturers (
    course_lecturer_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    lecturer_id INT NOT NULL,

    CONSTRAINT fk_cl_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_cl_lecturer
        FOREIGN KEY (lecturer_id)
        REFERENCES lecturers(lecturer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    UNIQUE(course_id, lecturer_id)
) ENGINE=InnoDB;


-- =====================================================
-- 10. COURSE MATERIALS
-- =====================================================

CREATE TABLE course_materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    lecturer_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    file_path VARCHAR(255),
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_material_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_material_lecturer
        FOREIGN KEY (lecturer_id)
        REFERENCES lecturers(lecturer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 11. ASSESSMENTS
-- =====================================================

CREATE TABLE assessments (
    assessment_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    assessment_type ENUM(
        'CA',
        'MID',
        'FINAL',
        'ASSIGNMENT',
        'PRACTICAL'
    ) NOT NULL,
    max_marks INT NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,

    CONSTRAINT fk_assessment_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 12. ENROLLMENTS
-- =====================================================

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    semester INT NOT NULL,
    status ENUM(
        'ENROLLED',
        'WITHDRAWN',
        'COMPLETED'
    ) DEFAULT 'ENROLLED',

    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id)
        REFERENCES undergraduates(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    UNIQUE(student_id, course_id, academic_year, semester)
) ENGINE=InnoDB;


-- =====================================================
-- 13. ATTENDANCE SESSIONS
-- =====================================================

CREATE TABLE attendance_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    session_code VARCHAR(50),
    component ENUM('THEORY','PRACTICAL') NOT NULL,
    session_number INT NOT NULL,
    total_sessions INT DEFAULT 1,
    session_date DATE NOT NULL,

    CONSTRAINT fk_session_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 14. ATTENDANCE
-- =====================================================

CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    student_id INT NOT NULL,
    medical_id INT NULL,
    status ENUM('PRESENT','ABSENT','LATE') NOT NULL,

    CONSTRAINT fk_attendance_session
        FOREIGN KEY (session_id)
        REFERENCES attendance_sessions(session_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_id)
        REFERENCES undergraduates(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    UNIQUE(session_id, student_id)
) ENGINE=InnoDB;


-- =====================================================
-- 15. MEDICALS
-- =====================================================

CREATE TABLE medicals (
    medical_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    medical_date DATE NOT NULL,
    reason TEXT,
    document_path VARCHAR(255),
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    approved_by INT NULL,
    approved_at DATETIME NULL,

    CONSTRAINT fk_medical_student
        FOREIGN KEY (student_id)
        REFERENCES undergraduates(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_medical_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_medical_approver
        FOREIGN KEY (approved_by)
        REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 16. MARKS
-- =====================================================

CREATE TABLE marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id INT NOT NULL,
    student_id INT NOT NULL,
    marks DECIMAL(5,2) NOT NULL,
    entered_by INT,
    entered_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_marks_assessment
        FOREIGN KEY (assessment_id)
        REFERENCES assessments(assessment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_marks_student
        FOREIGN KEY (student_id)
        REFERENCES undergraduates(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_marks_entered_by
        FOREIGN KEY (entered_by)
        REFERENCES users(user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    UNIQUE(assessment_id, student_id)
) ENGINE=InnoDB;


-- =====================================================
-- 17. GRADES
-- =====================================================

CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    student_id INT NOT NULL,
    total_marks DECIMAL(5,2),
    grade VARCHAR(2),
    grade_point DECIMAL(3,2),
    cgpa DECIMAL(3,2),

    CONSTRAINT fk_grade_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_grade_student
        FOREIGN KEY (student_id)
        REFERENCES undergraduates(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    UNIQUE(course_id, student_id)
) ENGINE=InnoDB;


-- =====================================================
-- 18. NOTICES
-- =====================================================

CREATE TABLE notices (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    created_by INT NOT NULL,
    target_role ENUM(
        'ALL',
        'LECTURER',
        'TO',
        'UNDERGRADUATE'
    ) DEFAULT 'ALL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',

    CONSTRAINT fk_notice_creator
        FOREIGN KEY (created_by)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 19. TIMETABLES
-- =====================================================

CREATE TABLE timetables (
    timetable_id INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NOT NULL,
    course_id INT NOT NULL,
    day VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(20),
    component ENUM('THEORY','PRACTICAL') NOT NULL,

    CONSTRAINT fk_timetable_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_timetable_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- SAMPLE DEPARTMENTS
-- =====================================================

INSERT INTO departments (department_name) VALUES
('Department of Information and Communication Technology'),
('Department of Engineering Technology'),
('Department of Biosystems Technology');


-- =====================================================
-- SAMPLE ADMIN USER
-- =====================================================

INSERT INTO users (username, password, role, status)
VALUES ('admin', 'admin123', 'ADMIN', 'ACTIVE');


-- =====================================================
-- CHECK TABLES
-- =====================================================

SHOW TABLES;