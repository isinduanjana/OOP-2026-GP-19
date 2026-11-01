package lk.ac.ruh.fot.ams.techofficer.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.dao.MedicalDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.techofficer.DuplicateMedicalException;
import lk.ac.ruh.fot.ams.techofficer.model.MedicalDecision;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Adds and maintains medical records and links
 * them to the matching absences (attendance.medical_id).
 *
 * Connection to common: "extends BaseDAO" (JDBC helpers + transactions) and
 * "implements CrudDAO<Medical>" (common generic CRUD interface); reuses the
 * common MedicalDAO.BASE_SELECT / mapMedical for reading.
 *
 * OOP: Inheritance, Abstraction (implements an interface), Polymorphism
 * (applyDecision works with any MedicalDecision), custom exception
 * (DuplicateMedicalException), JDBC transactions.
 */
public class TOMedicalDAO extends BaseDAO implements CrudDAO<Medical> {

    // ================================================================= read
    @Override
    public List<Medical> findAll() throws DatabaseException {
        return queryList(MedicalDAO.BASE_SELECT + "ORDER BY m.medical_date DESC, s.tg_no", MedicalDAO::mapMedical);
    }

    @Override
    public Medical findById(int id) throws DatabaseException {
        return queryOne(MedicalDAO.BASE_SELECT + "WHERE m.medical_id = ?", MedicalDAO::mapMedical, id);
    }

    public List<Medical> findPending() throws DatabaseException {
        return queryList(MedicalDAO.BASE_SELECT + "WHERE m.status = 'PENDING' ORDER BY m.medical_date, s.tg_no",
                MedicalDAO::mapMedical);
    }

    public int countByStatus(MedicalStatus status) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM medicals WHERE status = ?", status);
    }

    /** How many medicals this user approved / rejected. */
    public int countHandledBy(int userId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM medicals WHERE approved_by = ?", userId);
    }

    /** Number of absences a medical is linked to. */
    public int countLinkedAbsences(int medicalId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM attendance WHERE medical_id = ?", medicalId);
    }

    // ================================================================= write
    /** Adds a medical (validated) and links it to the absences of that day. Returns the new id. */
    @Override
    public int insert(Medical m) throws DatabaseException {
        // CrudDAO only allows DatabaseException, so the checked validation is done in add(...)
        int[] id = new int[1];
        inTransaction(c -> {
            id[0] = insert(c, "INSERT INTO medicals (student_id, course_id, medical_date, reason, document_path, status, "
                            + "approved_by, approved_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    m.getStudentId(), m.getCourseId(), m.getMedicalDate(), m.getReason(), m.getDocumentPath(),
                    m.getStatus(), m.getApprovedBy(), m.getApprovedAt());
            TOAttendanceDAO.relinkStudentDay(c, m.getStudentId(), m.getMedicalDate());
        });
        m.setMedicalId(id[0]);
        return id[0];
    }

    /** Validates (future date, duplicates) and then inserts. */
    public int add(Medical m) throws AppException {
        validate(m);
        return insert(m);
    }

    @Override
    public void update(Medical m) throws DatabaseException {
        Medical old = findById(m.getMedicalId());
        inTransaction(c -> {
            update(c, "UPDATE medicals SET student_id = ?, course_id = ?, medical_date = ?, reason = ?, document_path = ? "
                            + "WHERE medical_id = ?",
                    m.getStudentId(), m.getCourseId(), m.getMedicalDate(), m.getReason(), m.getDocumentPath(),
                    m.getMedicalId());
            // first free the old day's absences, then link the (possibly new) day
            update(c, "UPDATE attendance SET medical_id = NULL WHERE medical_id = ?", m.getMedicalId());
            if (old != null) TOAttendanceDAO.relinkStudentDay(c, old.getStudentId(), old.getMedicalDate());
            TOAttendanceDAO.relinkStudentDay(c, m.getStudentId(), m.getMedicalDate());
        });
    }

    /** Validates and then updates. */
    public void save(Medical m) throws AppException {
        validate(m);
        update(m);
    }

    @Override
    public void delete(int id) throws DatabaseException {
        Medical old = findById(id);
        inTransaction(c -> {
            update(c, "UPDATE attendance SET medical_id = NULL WHERE medical_id = ?", id);
            update(c, "DELETE FROM medicals WHERE medical_id = ?", id);
            // another medical of that day may now cover the absence
            if (old != null) TOAttendanceDAO.relinkStudentDay(c, old.getStudentId(), old.getMedicalDate());
        });
    }

    /**
     * Applies an approve / reject / reset decision (polymorphism: the DAO does not
     * know which concrete MedicalDecision it got).
     */
    public void applyDecision(Medical m, MedicalDecision decision, int deciderUserId) throws AppException {
        if (!decision.isAllowedFor(m)) {
            throw new ValidationException("Status", "This medical is already " + m.getStatus() + ".");
        }
        Integer by = decision.recordsDecisionMaker() ? deciderUserId : null;
        LocalDateTime at = decision.recordsDecisionMaker() ? LocalDateTime.now().withNano(0) : null;
        inTransaction(c -> {
            update(c, "UPDATE medicals SET status = ?, approved_by = ?, approved_at = ? WHERE medical_id = ?",
                    decision.getResultStatus(), by, at, m.getMedicalId());
            TOAttendanceDAO.relinkStudentDay(c, m.getStudentId(), m.getMedicalDate());
        });
        m.setStatus(decision.getResultStatus());
        m.setApprovedBy(by);
        m.setApprovedAt(at);
    }

    // ================================================================= validation
    private void validate(Medical m) throws AppException {
        if (m.getStudentId() <= 0) throw new ValidationException("Student", "Please choose a student.");
        if (m.getMedicalDate() == null) throw new ValidationException("Date", "Medical date is required.");
        if (m.getMedicalDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Date", "A medical cannot be added for a future date (" + m.getMedicalDate() + ").");
        }
        if (m.getMedicalDate().isBefore(LocalDate.now().minusYears(2))) {
            throw new ValidationException("Date", "The medical date is too old for the academic year "
                    + AppConfig.CURRENT_ACADEMIC_YEAR + ".");
        }
        if (m.getReason() == null || m.getReason().isBlank()) {
            throw new ValidationException("Reason", "Reason is required.");
        }
        if (m.getReason().length() > 1000) {
            throw new ValidationException("Reason", "Reason is too long (max 1000 characters).");
        }
        Integer existing = queryOne("SELECT medical_id FROM medicals WHERE student_id = ? AND medical_date = ? "
                        + "AND course_id <=> ? AND medical_id <> ?",
                rs -> rs.getInt(1), m.getStudentId(), m.getMedicalDate(), m.getCourseId(), m.getMedicalId());
        if (existing != null) {
            throw new DuplicateMedicalException(m.getTgNo(), m.getMedicalDate(),
                    m.getCourseId() == null ? "all courses" : m.getCourseCode(), existing);
        }
    }
}
