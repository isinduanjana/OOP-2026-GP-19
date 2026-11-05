package lk.ac.ruh.fot.ams.techofficer.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

/** Medical CRUD and validation for the Technical Officer module. */
public class TOMedicalDAO extends BaseDAO implements CrudDAO<Medical> {

    @Override
    public List<Medical> findAll() throws DatabaseException {
        return queryList(MedicalDAO.BASE_SELECT + "ORDER BY m.from_date DESC, s.tg_no", MedicalDAO::mapMedical);
    }

    @Override
    public Medical findById(int id) throws DatabaseException {
        return queryOne(MedicalDAO.BASE_SELECT + "WHERE m.medical_id = ?", MedicalDAO::mapMedical, id);
    }

    public List<Medical> findPending() throws DatabaseException {
        return queryList(MedicalDAO.BASE_SELECT + "WHERE m.status = 'PENDING' ORDER BY m.from_date, s.tg_no",
                MedicalDAO::mapMedical);
    }

    public int countByStatus(MedicalStatus status) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM medicals WHERE status = ?", status);
    }

    public int countHandledBy(int userId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM medicals WHERE approved_by = ?", userId);
    }

    public int countLinkedAbsences(int medicalId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM attendance WHERE medical_id = ?", medicalId);
    }

    /** Inserts a medical and links every absent session in its date range. */
    @Override
    public int insert(Medical m) throws DatabaseException {
        int[] id = new int[1];
        inTransaction(c -> {
            id[0] = insert(c,
                    "INSERT INTO medicals (student_id, course_id, medical_date, from_date, to_date, reason, "
                    + "document_path, status, approved_by, approved_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    m.getStudentId(), m.getCourseId(), m.getFromDate(), m.getFromDate(), m.getToDate(),
                    m.getReason(), m.getDocumentPath(), m.getStatus(), m.getApprovedBy(), m.getApprovedAt());
            TOAttendanceDAO.relinkStudentRange(c, m.getStudentId(), m.getFromDate(), m.getToDate());
        });
        m.setMedicalId(id[0]);
        return id[0];
    }

    /** New submissions must satisfy the 14-day deadline. */
    public int add(Medical m) throws AppException {
        validate(m, true);
        return insert(m);
    }

    @Override
    public void update(Medical m) throws DatabaseException {
        Medical old = findById(m.getMedicalId());
        inTransaction(c -> {
            update(c, "UPDATE medicals SET student_id = ?, course_id = ?, medical_date = ?, from_date = ?, "
                    + "to_date = ?, reason = ?, document_path = ? WHERE medical_id = ?",
                    m.getStudentId(), m.getCourseId(), m.getFromDate(), m.getFromDate(), m.getToDate(),
                    m.getReason(), m.getDocumentPath(), m.getMedicalId());
            update(c, "UPDATE attendance SET medical_id = NULL WHERE medical_id = ?", m.getMedicalId());
            if (old != null) {
                TOAttendanceDAO.relinkStudentRange(c, old.getStudentId(), old.getFromDate(), old.getToDate());
            }
            TOAttendanceDAO.relinkStudentRange(c, m.getStudentId(), m.getFromDate(), m.getToDate());
        });
    }

    /** Editing an existing record does not reset or re-apply the original submission deadline. */
    public void save(Medical m) throws AppException {
        validate(m, false);
        update(m);
    }

    @Override
    public void delete(int id) throws DatabaseException {
        Medical old = findById(id);
        inTransaction(c -> {
            update(c, "UPDATE attendance SET medical_id = NULL WHERE medical_id = ?", id);
            update(c, "DELETE FROM medicals WHERE medical_id = ?", id);
            if (old != null) {
                TOAttendanceDAO.relinkStudentRange(c, old.getStudentId(), old.getFromDate(), old.getToDate());
            }
        });
    }

    public void applyDecision(Medical m, MedicalDecision decision, int deciderUserId) throws AppException {
        if (!decision.isAllowedFor(m)) {
            throw new ValidationException("Status", "This medical is already " + m.getStatus() + ".");
        }
        Integer by = decision.recordsDecisionMaker() ? deciderUserId : null;
        LocalDateTime at = decision.recordsDecisionMaker() ? LocalDateTime.now().withNano(0) : null;
        inTransaction(c -> {
            update(c, "UPDATE medicals SET status = ?, approved_by = ?, approved_at = ? WHERE medical_id = ?",
                    decision.getResultStatus(), by, at, m.getMedicalId());
            TOAttendanceDAO.relinkStudentRange(c, m.getStudentId(), m.getFromDate(), m.getToDate());
        });
        m.setStatus(decision.getResultStatus());
        m.setApprovedBy(by);
        m.setApprovedAt(at);
    }

    private void validate(Medical m, boolean newSubmission) throws AppException {
        if (m.getStudentId() <= 0) throw new ValidationException("Student", "Please choose a student.");
        LocalDate from = m.getFromDate();
        LocalDate to = m.getToDate();
        LocalDate today = LocalDate.now();
        if (from == null || to == null) {
            throw new ValidationException("Date", "From Date and To Date are required.");
        }
        if (from.isAfter(to)) {
            throw new ValidationException("Date", "To Date cannot be before From Date.");
        }
        if (to.isAfter(today)) {
            throw new ValidationException("Date", "Medical dates cannot be in the future.");
        }
        if (newSubmission && from.isBefore(today.minusDays(14))) {
            throw new ValidationException("Invalid Medical",
                    "Medical submission period has expired. Submit the medical within 14 days from the first absent date.");
        }
        if (m.getReason() == null || m.getReason().isBlank()) {
            throw new ValidationException("Reason", "Reason is required.");
        }
        if (m.getReason().length() > 1000) {
            throw new ValidationException("Reason", "Reason is too long (max 1000 characters).");
        }

        Integer existing = queryOne(
                "SELECT medical_id FROM medicals WHERE student_id = ? AND from_date <= ? AND to_date >= ? "
                + "AND course_id <=> ? AND medical_id <> ? LIMIT 1",
                rs -> rs.getInt(1), m.getStudentId(), to, from, m.getCourseId(), m.getMedicalId());
        if (existing != null) {
            throw new DuplicateMedicalException(m.getTgNo(), from,
                    m.getCourseId() == null ? "all courses" : m.getCourseCode(), existing);
        }
    }
}
