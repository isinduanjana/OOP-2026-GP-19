# Faculty of Technology – Academic Management System

**ICT2132 – Object Oriented Programming Practicum · Mini Project · University of Ruhuna**

A Java Swing + MySQL desktop system for the Faculty of Technology that manages user profiles
(Admin, Lecturer, Technical Officer, Undergraduate), courses, marks, attendance, medicals,
notices and timetables — with CA eligibility, attendance eligibility (80 %), UGC grading,
SGPA and CGPA.

| | |
|---|---|
| Language | Java 17+ (Swing GUI, FlatLaf look & feel) |
| Database | MySQL 8 (database name `faculty_academic_system`, matches our ER diagram) |
| Build | Maven (`pom.xml`) – opens directly in IntelliJ IDEA / NetBeans / Eclipse |
| Main class | `lk.ac.ruh.fot.ams.Main` |

---

## 1. Run it (5 minutes)

1. Install **JDK 17 or newer** and **MySQL 8** (MySQL Server / XAMPP / WAMP all work).
2. Open `db.properties` and set your MySQL user and password:
   ```properties
   db.user=root
   db.password=your_mysql_password
   ```
3. Run the program:
   * **IntelliJ / NetBeans:** *Open* the project folder (it is a Maven project) → run `lk.ac.ruh.fot.ams.Main`.
   * **Command line:** `mvn clean package` then `java -jar target/fot-ams.jar`
4. On the first start the program **creates the database and loads the sample data automatically**
   (from `database/01_schema.sql` and `database/02_seed_data.sql`).
   You can also import those two files yourself in MySQL Workbench / phpMyAdmin.
   Use **Database settings → Reset sample data** on the login screen to restore the sample data any time.

### Sample logins (password for everyone: `1234`)

| Role | Usernames |
|---|---|
| Admin (M1) | `admin` |
| Lecturer (M2) | `lec.perera`, `lec.silva`, `lec.fernando`, `lec.jayasinghe`, `lec.wickrama`, `lec.bandara` |
| Technical Officer (M3) | `to.kamal`, `to.nimal`, `to.sunil`, `to.ruwan` |
| Undergraduate (M4) | `tg1001` … `tg1018` (normal), `tg0952`, `tg0967`, `tg0981` (batch missed), `tg0904`, `tg0915`, `tg0928` (repeat) |

---

## 2. How the project is divided

```
FOT-Academic-Management-System/
├── pom.xml, db.properties, README.md            COMMON
├── database/01_schema.sql, 02_seed_data.sql      COMMON
└── src/main/java/lk/ac/ruh/fot/ams/
    ├── Main.java                                  COMMON  entry point
    ├── config/  db/  exception/  util/            COMMON  settings, DB connection, exceptions, helpers
    ├── model/ (User → Staff → Admin/Lecturer/TO, User → Undergraduate, Course, …)   COMMON
    ├── dao/   (BaseDAO + read DAOs used by everyone)                               COMMON
    ├── service/ (AuthService, AttendanceService, ResultService)                    COMMON
    ├── ui/    (Theme, BaseDashboard, BasePanel, LoginFrame, components, shared pages) COMMON
    ├── admin/          M1  Admin
    ├── lecturer/       M2  Lecturer
    ├── techofficer/    M3  Technical Officer
    └── undergraduate/  M4  Undergraduate
```

| Zip | Contains | Owner |
|---|---|---|
| `Common.zip` | everything above marked COMMON (+ docs/OOP_Concepts.md, docs/GIT_WORKFLOW.md) | whole group (push first) |
| `M1.zip` | `src/main/java/lk/ac/ruh/fot/ams/admin/**`, `docs/M1_Admin.md` | M1 |
| `M2.zip` | `src/main/java/lk/ac/ruh/fot/ams/lecturer/**`, `docs/M2_Lecturer.md` | M2 |
| `M3.zip` | `src/main/java/lk/ac/ruh/fot/ams/techofficer/**`, `docs/M3_TechnicalOfficer.md` | M3 |
| `M4.zip` | `src/main/java/lk/ac/ruh/fot/ams/undergraduate/**`, `docs/M4_Undergraduate.md` | M4 |

Every zip has the same top folder `FOT-Academic-Management-System/`, so extracting all of them in the
same place merges them into one project. **How the parts plug together:** the common `LoginFrame`
asks `DashboardFactory` for the dashboard of the user's role. The factory loads
`admin.AdminDashboard`, `lecturer.LecturerDashboard`, `techofficer.TODashboard` or
`undergraduate.UndergraduateDashboard` *by name*, so the common part compiles and runs even before a
member's part is added (that role then shows "module not added yet"). See `docs/GIT_WORKFLOW.md`
for the exact push order.

---

## 3. Business rules implemented

| Rule (from the handout) | Where |
|---|---|
| 15 theory + 15 practical sessions, 1 session = 2 hours | `attendance_sessions`, `AppConfig` |
| Medical for a day: recorded, linked to the absences, Approved / Pending / Rejected | M3 Medicals, `medicals`, `attendance.medical_id` |
| Attendance ≥ 80 % (approved medicals count as attended) | `AttendanceSummary.isEligible()` |
| Theory only / Practical only / Combined (hours-weighted) attendance | `AttendanceSummary.getPercentage(...)`, Attendance Report |
| Batch summary and individual attendance | `AttendanceReportPanel` (M2, M3), M4 My Attendance |
| All marks out of 100; CA ≥ 40 % of CA part for eligibility | `ResultService`, `CourseResult.isCaEligible()` |
| Eligibility = attendance + CA, individual and whole batch | M2 CA & Eligibility |
| Final marks, marks, grades per subject – individual and batch | M2 Results & Grades, M4 Results |
| Grades per UGC Commission Circular No. 12‑2024 | `model/enums/GradeScale.java` |
| SGPA and CGPA with subjects and grades – individual and batch | `Transcript`, `SemesterResult`, M2 GPA page, M4 Results |

**Grading scale** (`GradeScale.java` – edit the numbers there if your circular copy differs):
A+ ≥85 (4.0) · A ≥75 (4.0) · A- ≥70 (3.7) · B+ ≥65 (3.3) · B ≥60 (3.0) · B- ≥55 (2.7) · C+ ≥50 (2.3) ·
C ≥45 (2.0) · C- ≥40 (1.7) · D+ ≥35 (1.3) · D ≥30 (1.0) · E <30 (0.0).
SGPA/CGPA = Σ(grade point × credits) / Σ credits.

**Assumptions (state these in the report):**
* Students who are not eligible (CA < 40 % or attendance < 80 %) do not sit the final exam and get **E** for that course.
* Repeat students' grade for a repeated course is capped at **C** (common faculty rule – remove in `ResultService` if not needed).
* A medical with no course covers every session on that date.
* The current-semester modules and evaluation criteria in the sample data are representative of Level 2 Semester 1;
  the Admin can change courses and assessment weights from the GUI (Course Management → Assessments).

---

## 4. Sample data – scenarios covered

* 1 admin, 6 lecturers, 4 technical officers, 24 undergraduates (18 normal, 3 repeat, 3 batch-missed)
* 6 current courses (ICT2113, ICT2122, ICT2132, ICT2142, ICT2152, ENG2122) + 10 previous-semester courses with grades (for CGPA)
* 2,985 attendance records, 54 medicals (approved, pending, rejected, whole-day), 495 marks

| Scenario | Example students |
|---|---|
| Attendance above 80 % | `tg1001`, `tg1002`, most others |
| Exactly 80 % | `tg1003` (all courses), `tg1015`, `tg0915`, `tg1004` in ICT2113, `tg1010` in ICT2132 |
| Below 80 % without medical | `tg1005` in ICT2113 & ICT2142, `tg1012` in ENG2122, `tg0981` in ICT2152 |
| ≥ 80 % only because of approved medicals | `tg1006` (whole-day medicals), `tg0952`, `tg1014` in ICT2152, `tg1017` in ICT2142 |
| Below 80 % even with medicals | `tg1007` in ICT2122 & ICT2132, `tg0928` |
| CA below 40 % (not eligible) | `tg1008` ICT2122, `tg1013` ICT2113, `tg0981` ICT2142, `tg1016` ENG2122 |
| CA exactly 40 % (eligible) | `tg1011` ICT2152 |
| Pending / rejected medical | `tg1007`, `tg0928` (pending), `tg1012` (rejected) |

---

## 5. OOP concepts

See `docs/OOP_Concepts.md` for the full table of where each concept appears in the common part
and in each member's part (M1–M4 each demonstrate classes & objects, inheritance, abstraction,
polymorphism, encapsulation, exception handling, database handling and GUI).

---

## 6. Troubleshooting

| Problem | Fix |
|---|---|
| "Cannot connect to MySQL server" | Start MySQL; check user/password in `db.properties` or *Database settings* on the login screen. |
| "Public Key Retrieval is not allowed" | Already handled in the JDBC URL (`allowPublicKeyRetrieval=true`). |
| Want fresh sample data | Login screen → *Database settings* → *Reset sample data*. |
| "M… module has not been added yet" | Extract that member's zip into the project folder and rebuild. |
| Course material / medical document "File not found" | Sample rows only store a file name – upload a real file from the GUI. |
