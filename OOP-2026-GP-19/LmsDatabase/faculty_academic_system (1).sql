-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Oct 15, 2026 at 07:56 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `faculty_academic_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `admin_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `employee_no` varchar(20) DEFAULT NULL,
  `department_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `assessments`
--

CREATE TABLE `assessments` (
  `assessment_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `assessment_type` enum('CA','MID','FINAL','ASSIGNMENT','PRACTICAL') NOT NULL,
  `max_marks` int(11) NOT NULL,
  `percentage` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `assessments`
--

INSERT INTO `assessments` (`assessment_id`, `course_id`, `assessment_type`, `max_marks`, `percentage`) VALUES
(1, 1, 'CA', 100, 20.00),
(2, 2, 'CA', 100, 20.00),
(3, 3, 'CA', 100, 20.00),
(4, 4, 'CA', 100, 20.00),
(5, 5, 'CA', 100, 20.00),
(6, 1, 'MID', 100, 20.00),
(7, 2, 'MID', 100, 20.00),
(8, 3, 'MID', 100, 20.00),
(9, 4, 'MID', 100, 20.00),
(10, 5, 'MID', 100, 20.00),
(11, 1, 'FINAL', 100, 40.00),
(12, 2, 'FINAL', 100, 40.00),
(13, 3, 'FINAL', 100, 40.00),
(14, 4, 'FINAL', 100, 40.00),
(15, 5, 'FINAL', 100, 40.00),
(16, 1, 'ASSIGNMENT', 100, 10.00),
(17, 2, 'ASSIGNMENT', 100, 10.00),
(18, 3, 'ASSIGNMENT', 100, 10.00),
(19, 4, 'ASSIGNMENT', 100, 10.00),
(20, 5, 'ASSIGNMENT', 100, 10.00),
(21, 1, 'PRACTICAL', 100, 10.00),
(22, 2, 'PRACTICAL', 100, 10.00),
(23, 3, 'PRACTICAL', 100, 10.00),
(24, 4, 'PRACTICAL', 100, 10.00),
(25, 5, 'PRACTICAL', 100, 10.00);

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendance_id` int(11) NOT NULL,
  `session_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `medical_id` int(11) DEFAULT NULL,
  `status` enum('PRESENT','ABSENT','LATE') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendance_id`, `session_id`, `student_id`, `medical_id`, `status`) VALUES
(1, 1, 2144, NULL, 'PRESENT'),
(2, 6, 2144, NULL, 'PRESENT'),
(3, 11, 2144, NULL, 'PRESENT'),
(4, 16, 2144, NULL, 'ABSENT'),
(5, 21, 2144, NULL, 'PRESENT'),
(6, 26, 2144, NULL, 'PRESENT'),
(7, 31, 2144, NULL, 'PRESENT'),
(8, 36, 2144, NULL, 'ABSENT'),
(9, 64, 2144, NULL, 'PRESENT'),
(10, 67, 2144, NULL, 'PRESENT'),
(11, 70, 2144, NULL, 'LATE'),
(12, 73, 2144, NULL, 'PRESENT'),
(13, 2, 2144, NULL, 'PRESENT'),
(14, 7, 2144, NULL, 'PRESENT'),
(15, 12, 2144, NULL, 'PRESENT'),
(16, 17, 2144, NULL, 'ABSENT'),
(17, 22, 2144, NULL, 'PRESENT'),
(18, 27, 2144, NULL, 'PRESENT'),
(19, 32, 2144, NULL, 'PRESENT'),
(20, 37, 2144, NULL, 'ABSENT'),
(21, 65, 2144, NULL, 'PRESENT'),
(22, 68, 2144, NULL, 'PRESENT'),
(23, 71, 2144, NULL, 'LATE'),
(24, 74, 2144, NULL, 'PRESENT'),
(25, 3, 2144, NULL, 'PRESENT'),
(26, 8, 2144, NULL, 'PRESENT'),
(27, 13, 2144, NULL, 'PRESENT'),
(28, 18, 2144, NULL, 'ABSENT'),
(29, 23, 2144, NULL, 'PRESENT'),
(30, 28, 2144, NULL, 'PRESENT'),
(31, 33, 2144, NULL, 'PRESENT'),
(32, 38, 2144, NULL, 'ABSENT'),
(33, 66, 2144, NULL, 'PRESENT'),
(34, 69, 2144, NULL, 'PRESENT'),
(35, 72, 2144, NULL, 'LATE'),
(36, 75, 2144, NULL, 'PRESENT'),
(37, 4, 2144, NULL, 'PRESENT'),
(38, 9, 2144, NULL, 'PRESENT'),
(39, 14, 2144, NULL, 'PRESENT'),
(40, 19, 2144, NULL, 'ABSENT'),
(41, 24, 2144, NULL, 'PRESENT'),
(42, 29, 2144, NULL, 'PRESENT'),
(43, 34, 2144, NULL, 'PRESENT'),
(44, 39, 2144, NULL, 'ABSENT'),
(45, 5, 2144, NULL, 'PRESENT'),
(46, 10, 2144, NULL, 'PRESENT'),
(47, 15, 2144, NULL, 'PRESENT'),
(48, 20, 2144, NULL, 'ABSENT'),
(49, 25, 2144, NULL, 'PRESENT'),
(50, 30, 2144, NULL, 'PRESENT'),
(51, 35, 2144, NULL, 'PRESENT'),
(52, 40, 2144, NULL, 'ABSENT');

-- --------------------------------------------------------

--
-- Table structure for table `attendance_sessions`
--

CREATE TABLE `attendance_sessions` (
  `session_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `session_code` varchar(50) DEFAULT NULL,
  `component` enum('THEORY','PRACTICAL') NOT NULL,
  `session_number` int(11) NOT NULL,
  `total_sessions` int(11) DEFAULT 1,
  `session_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `attendance_sessions`
--

INSERT INTO `attendance_sessions` (`session_id`, `course_id`, `session_code`, `component`, `session_number`, `total_sessions`, `session_date`) VALUES
(1, 1, 'ICT1201-TH-1', 'THEORY', 1, 8, '2026-08-03'),
(2, 2, 'ICT1202-TH-1', 'THEORY', 1, 8, '2026-08-03'),
(3, 3, 'ICT1203-TH-1', 'THEORY', 1, 8, '2026-08-03'),
(4, 4, 'ICT1204-TH-1', 'THEORY', 1, 8, '2026-08-03'),
(5, 5, 'ICT1205-TH-1', 'THEORY', 1, 8, '2026-08-03'),
(6, 1, 'ICT1201-TH-2', 'THEORY', 2, 8, '2026-08-10'),
(7, 2, 'ICT1202-TH-2', 'THEORY', 2, 8, '2026-08-10'),
(8, 3, 'ICT1203-TH-2', 'THEORY', 2, 8, '2026-08-10'),
(9, 4, 'ICT1204-TH-2', 'THEORY', 2, 8, '2026-08-10'),
(10, 5, 'ICT1205-TH-2', 'THEORY', 2, 8, '2026-08-10'),
(11, 1, 'ICT1201-TH-3', 'THEORY', 3, 8, '2026-08-17'),
(12, 2, 'ICT1202-TH-3', 'THEORY', 3, 8, '2026-08-17'),
(13, 3, 'ICT1203-TH-3', 'THEORY', 3, 8, '2026-08-17'),
(14, 4, 'ICT1204-TH-3', 'THEORY', 3, 8, '2026-08-17'),
(15, 5, 'ICT1205-TH-3', 'THEORY', 3, 8, '2026-08-17'),
(16, 1, 'ICT1201-TH-4', 'THEORY', 4, 8, '2026-08-24'),
(17, 2, 'ICT1202-TH-4', 'THEORY', 4, 8, '2026-08-24'),
(18, 3, 'ICT1203-TH-4', 'THEORY', 4, 8, '2026-08-24'),
(19, 4, 'ICT1204-TH-4', 'THEORY', 4, 8, '2026-08-24'),
(20, 5, 'ICT1205-TH-4', 'THEORY', 4, 8, '2026-08-24'),
(21, 1, 'ICT1201-TH-5', 'THEORY', 5, 8, '2026-08-31'),
(22, 2, 'ICT1202-TH-5', 'THEORY', 5, 8, '2026-08-31'),
(23, 3, 'ICT1203-TH-5', 'THEORY', 5, 8, '2026-08-31'),
(24, 4, 'ICT1204-TH-5', 'THEORY', 5, 8, '2026-08-31'),
(25, 5, 'ICT1205-TH-5', 'THEORY', 5, 8, '2026-08-31'),
(26, 1, 'ICT1201-TH-6', 'THEORY', 6, 8, '2026-09-07'),
(27, 2, 'ICT1202-TH-6', 'THEORY', 6, 8, '2026-09-07'),
(28, 3, 'ICT1203-TH-6', 'THEORY', 6, 8, '2026-09-07'),
(29, 4, 'ICT1204-TH-6', 'THEORY', 6, 8, '2026-09-07'),
(30, 5, 'ICT1205-TH-6', 'THEORY', 6, 8, '2026-09-07'),
(31, 1, 'ICT1201-TH-7', 'THEORY', 7, 8, '2026-09-14'),
(32, 2, 'ICT1202-TH-7', 'THEORY', 7, 8, '2026-09-14'),
(33, 3, 'ICT1203-TH-7', 'THEORY', 7, 8, '2026-09-14'),
(34, 4, 'ICT1204-TH-7', 'THEORY', 7, 8, '2026-09-14'),
(35, 5, 'ICT1205-TH-7', 'THEORY', 7, 8, '2026-09-14'),
(36, 1, 'ICT1201-TH-8', 'THEORY', 8, 8, '2026-09-21'),
(37, 2, 'ICT1202-TH-8', 'THEORY', 8, 8, '2026-09-21'),
(38, 3, 'ICT1203-TH-8', 'THEORY', 8, 8, '2026-09-21'),
(39, 4, 'ICT1204-TH-8', 'THEORY', 8, 8, '2026-09-21'),
(40, 5, 'ICT1205-TH-8', 'THEORY', 8, 8, '2026-09-21'),
(64, 1, 'ICT1201-PR-1', 'PRACTICAL', 1, 4, '2026-08-05'),
(65, 2, 'ICT1202-PR-1', 'PRACTICAL', 1, 4, '2026-08-05'),
(66, 3, 'ICT1203-PR-1', 'PRACTICAL', 1, 4, '2026-08-05'),
(67, 1, 'ICT1201-PR-2', 'PRACTICAL', 2, 4, '2026-08-19'),
(68, 2, 'ICT1202-PR-2', 'PRACTICAL', 2, 4, '2026-08-19'),
(69, 3, 'ICT1203-PR-2', 'PRACTICAL', 2, 4, '2026-08-19'),
(70, 1, 'ICT1201-PR-3', 'PRACTICAL', 3, 4, '2026-09-02'),
(71, 2, 'ICT1202-PR-3', 'PRACTICAL', 3, 4, '2026-09-02'),
(72, 3, 'ICT1203-PR-3', 'PRACTICAL', 3, 4, '2026-09-02'),
(73, 1, 'ICT1201-PR-4', 'PRACTICAL', 4, 4, '2026-09-16'),
(74, 2, 'ICT1202-PR-4', 'PRACTICAL', 4, 4, '2026-09-16'),
(75, 3, 'ICT1203-PR-4', 'PRACTICAL', 4, 4, '2026-09-16');

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE `courses` (
  `course_id` int(11) NOT NULL,
  `course_code` varchar(20) NOT NULL,
  `course_name` varchar(100) NOT NULL,
  `credits` int(11) NOT NULL,
  `theory_hours` int(11) DEFAULT 0,
  `practical_hours` int(11) DEFAULT 0,
  `description` text DEFAULT NULL,
  `department_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `courses`
--

INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `theory_hours`, `practical_hours`, `description`, `department_id`) VALUES
(1, 'ICT1201', 'Programming Fundamentals', 3, 2, 2, 'Demo course for testing marks and attendance', 1),
(2, 'ICT1202', 'Database Management Systems', 3, 2, 2, 'Demo course for testing marks and attendance', 1),
(3, 'ICT1203', 'Web Development', 3, 2, 2, 'Demo course for testing marks and attendance', 1),
(4, 'ICT1204', 'Computer Networks', 2, 2, 0, 'Demo course for testing marks and attendance', 1),
(5, 'ICT1205', 'Mathematics for ICT', 2, 2, 0, 'Demo course for testing marks and attendance', 1);

-- --------------------------------------------------------

--
-- Table structure for table `course_lecturers`
--

CREATE TABLE `course_lecturers` (
  `course_lecturer_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `lecturer_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `course_materials`
--

CREATE TABLE `course_materials` (
  `material_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `lecturer_id` int(11) NOT NULL,
  `title` varchar(100) NOT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `uploaded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `department_id` int(11) NOT NULL,
  `department_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`department_id`, `department_name`) VALUES
(3, 'Department of Biosystems Technology'),
(2, 'Department of Engineering Technology'),
(1, 'Department of Information and Communication Technology');

-- --------------------------------------------------------

--
-- Table structure for table `enrollments`
--

CREATE TABLE `enrollments` (
  `enrollment_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `academic_year` varchar(9) NOT NULL,
  `semester` int(11) NOT NULL,
  `status` enum('ENROLLED','WITHDRAWN','COMPLETED') DEFAULT 'ENROLLED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `enrollments`
--

INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `status`) VALUES
(1, 2144, 1, '2026/2027', 2, 'ENROLLED'),
(2, 2144, 2, '2026/2027', 2, 'ENROLLED'),
(3, 2144, 3, '2026/2027', 2, 'ENROLLED'),
(4, 2144, 4, '2026/2027', 2, 'ENROLLED'),
(5, 2144, 5, '2026/2027', 2, 'ENROLLED');

-- --------------------------------------------------------

--
-- Table structure for table `grades`
--

CREATE TABLE `grades` (
  `grade_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `total_marks` decimal(5,2) DEFAULT NULL,
  `grade` varchar(2) DEFAULT NULL,
  `grade_point` decimal(3,2) DEFAULT NULL,
  `cgpa` decimal(3,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `grades`
--

INSERT INTO `grades` (`grade_id`, `course_id`, `student_id`, `total_marks`, `grade`, `grade_point`, `cgpa`) VALUES
(1, 1, 2144, 80.40, 'B', 3.00, NULL),
(2, 2, 2144, 76.20, 'B', 3.00, NULL),
(3, 3, 2144, 89.80, 'A', 4.00, NULL),
(4, 4, 2144, 70.40, 'B', 3.00, NULL),
(5, 5, 2144, 81.80, 'B', 3.00, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `lecturers`
--

CREATE TABLE `lecturers` (
  `lecturer_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `employee_no` varchar(20) DEFAULT NULL,
  `department_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `marks`
--

CREATE TABLE `marks` (
  `mark_id` int(11) NOT NULL,
  `assessment_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `marks` decimal(5,2) NOT NULL,
  `entered_by` int(11) DEFAULT NULL,
  `entered_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `marks`
--

INSERT INTO `marks` (`mark_id`, `assessment_id`, `student_id`, `marks`, `entered_by`, `entered_at`) VALUES
(1, 1, 2144, 82.00, 2144, '2026-10-15 02:20:13'),
(2, 6, 2144, 76.00, 2144, '2026-10-15 02:20:13'),
(3, 11, 2144, 79.00, 2144, '2026-10-15 02:20:13'),
(4, 16, 2144, 88.00, 2144, '2026-10-15 02:20:13'),
(5, 21, 2144, 84.00, 2144, '2026-10-15 02:20:13'),
(6, 2, 2144, 75.00, 2144, '2026-10-15 02:20:13'),
(7, 7, 2144, 81.00, 2144, '2026-10-15 02:20:13'),
(8, 12, 2144, 73.00, 2144, '2026-10-15 02:20:13'),
(9, 17, 2144, 80.00, 2144, '2026-10-15 02:20:13'),
(10, 22, 2144, 78.00, 2144, '2026-10-15 02:20:13'),
(11, 3, 2144, 91.00, 2144, '2026-10-15 02:20:13'),
(12, 8, 2144, 87.00, 2144, '2026-10-15 02:20:13'),
(13, 13, 2144, 89.00, 2144, '2026-10-15 02:20:13'),
(14, 18, 2144, 94.00, 2144, '2026-10-15 02:20:13'),
(15, 23, 2144, 92.00, 2144, '2026-10-15 02:20:13'),
(16, 4, 2144, 68.00, 2144, '2026-10-15 02:20:13'),
(17, 9, 2144, 72.00, 2144, '2026-10-15 02:20:13'),
(18, 14, 2144, 70.00, 2144, '2026-10-15 02:20:13'),
(19, 19, 2144, 74.00, 2144, '2026-10-15 02:20:13'),
(20, 24, 2144, 70.00, 2144, '2026-10-15 02:20:13'),
(21, 5, 2144, 84.00, 2144, '2026-10-15 02:20:13'),
(22, 10, 2144, 78.00, 2144, '2026-10-15 02:20:13'),
(23, 15, 2144, 82.00, 2144, '2026-10-15 02:20:13'),
(24, 20, 2144, 86.00, 2144, '2026-10-15 02:20:13'),
(25, 25, 2144, 80.00, 2144, '2026-10-15 02:20:13');

-- --------------------------------------------------------

--
-- Table structure for table `medicals`
--

CREATE TABLE `medicals` (
  `medical_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `medical_date` date NOT NULL,
  `reason` text DEFAULT NULL,
  `document_path` varchar(255) DEFAULT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  `approved_by` int(11) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notices`
--

CREATE TABLE `notices` (
  `notice_id` int(11) NOT NULL,
  `title` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `created_by` int(11) NOT NULL,
  `target_role` enum('ALL','LECTURER','TO','UNDERGRADUATE') DEFAULT 'ALL',
  `created_at` datetime DEFAULT current_timestamp(),
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tech_officers`
--

CREATE TABLE `tech_officers` (
  `to_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `employee_no` varchar(20) DEFAULT NULL,
  `department_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `timetables`
--

CREATE TABLE `timetables` (
  `timetable_id` int(11) NOT NULL,
  `department_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `day` varchar(10) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `room` varchar(20) DEFAULT NULL,
  `component` enum('THEORY','PRACTICAL') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `undergraduates`
--

CREATE TABLE `undergraduates` (
  `student_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `tg_no` varchar(20) NOT NULL,
  `batch` varchar(10) DEFAULT NULL,
  `semester` int(11) DEFAULT NULL,
  `student_type` enum('NORMAL','REPEAT','BATCH_MISSED') DEFAULT 'NORMAL',
  `department_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `undergraduates`
--

INSERT INTO `undergraduates` (`student_id`, `user_id`, `tg_no`, `batch`, `semester`, `student_type`, `department_id`) VALUES
(2144, 2144, 'tg2144', '9', 2, 'NORMAL', 1);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','LECTURER','TO','UNDERGRADUATE') NOT NULL,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`, `status`, `created_at`) VALUES
(1, 'admin', 'admin123', 'ADMIN', 'ACTIVE', '2026-10-12 15:03:56'),
(2, 'un', 'un123', 'ADMIN', 'ACTIVE', '2026-10-15 01:52:30'),
(2144, '2144', '2144', 'UNDERGRADUATE', 'ACTIVE', '2026-10-15 02:03:48');

-- --------------------------------------------------------

--
-- Table structure for table `user_profiles`
--

CREATE TABLE `user_profiles` (
  `profile_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `profile_picture` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user_profiles`
--

INSERT INTO `user_profiles` (`profile_id`, `user_id`, `full_name`, `email`, `phone`, `address`, `profile_picture`) VALUES
(1, 2144, 'Demo Student TG2144', 'tg2144.demo@example.com', '0771234567', 'Demo Address, Sri Lanka', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`admin_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `employee_no` (`employee_no`),
  ADD KEY `fk_admin_department` (`department_id`);

--
-- Indexes for table `assessments`
--
ALTER TABLE `assessments`
  ADD PRIMARY KEY (`assessment_id`),
  ADD KEY `fk_assessment_course` (`course_id`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendance_id`),
  ADD UNIQUE KEY `session_id` (`session_id`,`student_id`),
  ADD KEY `fk_attendance_student` (`student_id`);

--
-- Indexes for table `attendance_sessions`
--
ALTER TABLE `attendance_sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD KEY `fk_session_course` (`course_id`);

--
-- Indexes for table `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`course_id`),
  ADD UNIQUE KEY `course_code` (`course_code`),
  ADD KEY `fk_course_department` (`department_id`);

--
-- Indexes for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  ADD PRIMARY KEY (`course_lecturer_id`),
  ADD UNIQUE KEY `course_id` (`course_id`,`lecturer_id`),
  ADD KEY `fk_cl_lecturer` (`lecturer_id`);

--
-- Indexes for table `course_materials`
--
ALTER TABLE `course_materials`
  ADD PRIMARY KEY (`material_id`),
  ADD KEY `fk_material_course` (`course_id`),
  ADD KEY `fk_material_lecturer` (`lecturer_id`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`department_id`),
  ADD UNIQUE KEY `department_name` (`department_name`);

--
-- Indexes for table `enrollments`
--
ALTER TABLE `enrollments`
  ADD PRIMARY KEY (`enrollment_id`),
  ADD UNIQUE KEY `student_id` (`student_id`,`course_id`,`academic_year`,`semester`),
  ADD KEY `fk_enrollment_course` (`course_id`);

--
-- Indexes for table `grades`
--
ALTER TABLE `grades`
  ADD PRIMARY KEY (`grade_id`),
  ADD UNIQUE KEY `course_id` (`course_id`,`student_id`),
  ADD KEY `fk_grade_student` (`student_id`);

--
-- Indexes for table `lecturers`
--
ALTER TABLE `lecturers`
  ADD PRIMARY KEY (`lecturer_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `employee_no` (`employee_no`),
  ADD KEY `fk_lecturer_department` (`department_id`);

--
-- Indexes for table `marks`
--
ALTER TABLE `marks`
  ADD PRIMARY KEY (`mark_id`),
  ADD UNIQUE KEY `assessment_id` (`assessment_id`,`student_id`),
  ADD KEY `fk_marks_student` (`student_id`),
  ADD KEY `fk_marks_entered_by` (`entered_by`);

--
-- Indexes for table `medicals`
--
ALTER TABLE `medicals`
  ADD PRIMARY KEY (`medical_id`),
  ADD KEY `fk_medical_student` (`student_id`),
  ADD KEY `fk_medical_course` (`course_id`),
  ADD KEY `fk_medical_approver` (`approved_by`);

--
-- Indexes for table `notices`
--
ALTER TABLE `notices`
  ADD PRIMARY KEY (`notice_id`),
  ADD KEY `fk_notice_creator` (`created_by`);

--
-- Indexes for table `tech_officers`
--
ALTER TABLE `tech_officers`
  ADD PRIMARY KEY (`to_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `employee_no` (`employee_no`),
  ADD KEY `fk_to_department` (`department_id`);

--
-- Indexes for table `timetables`
--
ALTER TABLE `timetables`
  ADD PRIMARY KEY (`timetable_id`),
  ADD KEY `fk_timetable_department` (`department_id`),
  ADD KEY `fk_timetable_course` (`course_id`);

--
-- Indexes for table `undergraduates`
--
ALTER TABLE `undergraduates`
  ADD PRIMARY KEY (`student_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `tg_no` (`tg_no`),
  ADD KEY `fk_student_department` (`department_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `user_profiles`
--
ALTER TABLE `user_profiles`
  ADD PRIMARY KEY (`profile_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `admin_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `assessments`
--
ALTER TABLE `assessments`
  MODIFY `assessment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `attendance_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=64;

--
-- AUTO_INCREMENT for table `attendance_sessions`
--
ALTER TABLE `attendance_sessions`
  MODIFY `session_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=79;

--
-- AUTO_INCREMENT for table `courses`
--
ALTER TABLE `courses`
  MODIFY `course_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  MODIFY `course_lecturer_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `course_materials`
--
ALTER TABLE `course_materials`
  MODIFY `material_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `department_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `enrollments`
--
ALTER TABLE `enrollments`
  MODIFY `enrollment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `grades`
--
ALTER TABLE `grades`
  MODIFY `grade_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `lecturers`
--
ALTER TABLE `lecturers`
  MODIFY `lecturer_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `marks`
--
ALTER TABLE `marks`
  MODIFY `mark_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `medicals`
--
ALTER TABLE `medicals`
  MODIFY `medical_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notices`
--
ALTER TABLE `notices`
  MODIFY `notice_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `tech_officers`
--
ALTER TABLE `tech_officers`
  MODIFY `to_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `timetables`
--
ALTER TABLE `timetables`
  MODIFY `timetable_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `undergraduates`
--
ALTER TABLE `undergraduates`
  MODIFY `student_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2145;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2145;

--
-- AUTO_INCREMENT for table `user_profiles`
--
ALTER TABLE `user_profiles`
  MODIFY `profile_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admins`
--
ALTER TABLE `admins`
  ADD CONSTRAINT `fk_admin_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `assessments`
--
ALTER TABLE `assessments`
  ADD CONSTRAINT `fk_assessment_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `fk_attendance_session` FOREIGN KEY (`session_id`) REFERENCES `attendance_sessions` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_attendance_student` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `attendance_sessions`
--
ALTER TABLE `attendance_sessions`
  ADD CONSTRAINT `fk_session_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `courses`
--
ALTER TABLE `courses`
  ADD CONSTRAINT `fk_course_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  ADD CONSTRAINT `fk_cl_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_cl_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `course_materials`
--
ALTER TABLE `course_materials`
  ADD CONSTRAINT `fk_material_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_material_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `enrollments`
--
ALTER TABLE `enrollments`
  ADD CONSTRAINT `fk_enrollment_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `grades`
--
ALTER TABLE `grades`
  ADD CONSTRAINT `fk_grade_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `lecturers`
--
ALTER TABLE `lecturers`
  ADD CONSTRAINT `fk_lecturer_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_lecturer_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `marks`
--
ALTER TABLE `marks`
  ADD CONSTRAINT `fk_marks_assessment` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`assessment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_marks_entered_by` FOREIGN KEY (`entered_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_marks_student` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `medicals`
--
ALTER TABLE `medicals`
  ADD CONSTRAINT `fk_medical_approver` FOREIGN KEY (`approved_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_medical_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_medical_student` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `notices`
--
ALTER TABLE `notices`
  ADD CONSTRAINT `fk_notice_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `tech_officers`
--
ALTER TABLE `tech_officers`
  ADD CONSTRAINT `fk_to_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_to_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `timetables`
--
ALTER TABLE `timetables`
  ADD CONSTRAINT `fk_timetable_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_timetable_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `undergraduates`
--
ALTER TABLE `undergraduates`
  ADD CONSTRAINT `fk_student_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user_profiles`
--
ALTER TABLE `user_profiles`
  ADD CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
