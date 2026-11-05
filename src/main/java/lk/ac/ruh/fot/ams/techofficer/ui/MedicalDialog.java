package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.techofficer.dao.TOMedicalDAO;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Add / edit a medical record.
 * Student (searchable, sorted by TG no), course or "All courses that day",
 * date (no future dates), reason and an optional scanned document that is
 * copied into uploads/medicals with the common FileUtil.
 *
 * OOP: Inheritance (extends RecordEntryDialog - Template Method), Polymorphism
 * (its buildForm/readForm/save are called by the parent's open()),
 * Exception handling (DuplicateMedicalException is thrown by TOMedicalDAO).
 */
public class MedicalDialog extends RecordEntryDialog<Medical> {

    /** First item of the course combo = medical covers every course of that day. */
    private static final String ALL_COURSES = "All courses that day";

    private final TOMedicalDAO dao;
    private final CourseDAO courseDAO = new CourseDAO();
    private final User officer;
    private final List<Undergraduate> students;
    private final Medical existing;             // null = new medical

    private final JTextField studentSearch = UI.searchField("Filter by TG no or name");
    private final JComboBox<Undergraduate> studentBox = new JComboBox<>();
    private final JComboBox<Object> courseBox = new JComboBox<>();
    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;
    private final JLabel submissionDateLabel = UI.muted(LocalDate.now().toString());
    private final JTextArea reasonArea = UI.textArea(3);
    private final JLabel documentLabel = UI.muted("No document attached");
    private final JCheckBox approveNow = new JCheckBox("Approve immediately (document checked)");
    private String documentPath;

    public MedicalDialog(Component parent, Color accent, TOMedicalDAO dao, User officer,
                         List<Undergraduate> students, Medical existing) {
        super(parent, existing == null ? "Add medical" : "Edit medical", accent);
        this.dao = dao;
        this.officer = officer;
        this.students = new ArrayList<>(students);
        this.students.sort((a, b) -> a.getTgNo().compareToIgnoreCase(b.getTgNo()));
        this.existing = existing;
        this.documentPath = existing == null ? null : existing.getDocumentPath();
    }

    @Override
    protected String getHeading() {
        return existing == null ? "Record a medical submission" : "Edit medical #" + existing.getMedicalId();
    }

    @Override
    protected String getIconName() {
        return "medical";
    }

    @Override
    protected JComponent buildForm() {
        studentBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Undergraduate u) setText(u.getTgNo() + "  -  " + u.getFullName());
                return this;
            }
        });
        fillStudents("");
        studentSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { fillStudents(studentSearch.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { fillStudents(studentSearch.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { fillStudents(studentSearch.getText()); }
        });
        studentBox.addActionListener(e -> fillCourses());
        if (existing != null) {
            for (int i = 0; i < studentBox.getItemCount(); i++) {
                if (studentBox.getItemAt(i).getStudentId() == existing.getStudentId()) studentBox.setSelectedIndex(i);
            }
        }
        fillCourses();

        fromDateSpinner = dateSpinner(existing == null ? null : existing.getFromDate());
        toDateSpinner = dateSpinner(existing == null ? null : existing.getToDate());
        if (existing != null && existing.getSubmittedAt() != null) {
            submissionDateLabel.setText(existing.getSubmittedAt().toLocalDate().toString());
        }
        if (existing != null) reasonArea.setText(existing.getReason());
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(360, 72));

        ModernButton choose = UI.secondary("Attach file", "upload");
        choose.addActionListener(e -> chooseDocument());
        ModernButton remove = UI.secondary("Remove", "trash");
        remove.addActionListener(e -> {
            documentPath = null;
            showDocument();
        });
        showDocument();
        JPanel docRow = new JPanel(new BorderLayout(8, 0));
        docRow.setOpaque(false);
        docRow.add(documentLabel, BorderLayout.CENTER);
        docRow.add(UI.row(choose, remove), BorderLayout.EAST);

        for (JComponent c : new JComponent[]{studentSearch, studentBox, courseBox, fromDateSpinner, toDateSpinner, submissionDateLabel}) {
            c.setPreferredSize(new Dimension(360, 34));
        }
        approveNow.setOpaque(false);
        approveNow.setVisible(existing == null);

        return UI.form(new String[]{"Search", "Student", "Course", "From Date", "To Date", "Submission Date", "Reason", "Document", ""},
                studentSearch, studentBox, courseBox, fromDateSpinner, toDateSpinner,
                submissionDateLabel, reasonScroll, docRow, approveNow);
    }

    /** Fills the student combo with students matching the search text. */
    private void fillStudents(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        Undergraduate selected = (Undergraduate) studentBox.getSelectedItem();
        DefaultComboBoxModel<Undergraduate> m = new DefaultComboBoxModel<>();
        for (Undergraduate u : students) {
            if (q.isEmpty() || u.getTgNo().toLowerCase().contains(q) || u.getFullName().toLowerCase().contains(q)) {
                m.addElement(u);
            }
        }
        studentBox.setModel(m);
        if (selected != null && m.getIndexOf(selected) >= 0) studentBox.setSelectedItem(selected);
        fillCourses();
    }

    /** Courses of the selected student this year (+ the medical's own course when editing). */
    private void fillCourses() {
        Object prev = courseBox.getSelectedItem();
        DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<>();
        m.addElement(ALL_COURSES);
        Undergraduate u = (Undergraduate) studentBox.getSelectedItem();
        try {
            if (u != null) {
                for (Course c : courseDAO.findByStudent(u.getStudentId())) m.addElement(c);
            }
            if (existing != null && existing.getCourseId() != null) {
                Course c = courseDAO.findById(existing.getCourseId());
                if (c != null && m.getIndexOf(c) < 0) m.addElement(c);
            }
        } catch (AppException e) {
            UI.error(this, e);
        }
        courseBox.setModel(m);
        if (prev != null && m.getIndexOf(prev) >= 0) {
            courseBox.setSelectedItem(prev);
        } else if (existing != null && existing.getCourseId() != null) {
            for (int i = 0; i < m.getSize(); i++) {
                if (m.getElementAt(i) instanceof Course c && c.getCourseId() == existing.getCourseId()) {
                    courseBox.setSelectedIndex(i);
                }
            }
        }
    }

    private void chooseDocument() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Medical documents (pdf, png, jpg)", "pdf", "png", "jpg", "jpeg"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            documentPath = FileUtil.copyToUploads(f, FileUtil.MEDICALS);   // common helper
            showDocument();
        } catch (AppException e) {
            UI.error(this, e);
        }
    }

    private void showDocument() {
        if (documentPath == null || documentPath.isBlank()) {
            documentLabel.setText("No document attached");
            documentLabel.setForeground(Theme.TEXT_MUTED);
        } else {
            documentLabel.setText(new File(documentPath).getName());
            documentLabel.setForeground(Theme.TEXT);
            documentLabel.setToolTipText(documentPath);
        }
    }

    @Override
    protected Medical readForm() throws ValidationException {
        Undergraduate u = (Undergraduate) studentBox.getSelectedItem();
        if (u == null) throw new ValidationException("Student", "Please choose a student.");
        Medical m = new Medical();
        if (existing != null) {
            m.setMedicalId(existing.getMedicalId());
            m.setStatus(existing.getStatus());
            m.setApprovedBy(existing.getApprovedBy());
            m.setApprovedAt(existing.getApprovedAt());
        }
        m.setStudentId(u.getStudentId());
        m.setTgNo(u.getTgNo());
        m.setStudentName(u.getFullName());
        Object c = courseBox.getSelectedItem();
        if (c instanceof Course course) {
            m.setCourseId(course.getCourseId());
            m.setCourseCode(course.getCourseCode());
        }
        LocalDate fromDate = readDate(fromDateSpinner);
        LocalDate toDate = readDate(toDateSpinner);
        if (fromDate.isAfter(toDate)) {
            throw new ValidationException("Date", "To Date cannot be before From Date.");
        }
        if (toDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Date", "Medical dates cannot be in the future.");
        }
        m.setFromDate(fromDate);
        m.setToDate(toDate);
        m.setMedicalDate(fromDate); // keep compatibility with existing screens
        m.setReason(Validator.required(reasonArea.getText(), "Reason"));
        m.setDocumentPath(documentPath);
        if (existing == null && approveNow.isSelected()) {
            m.setStatus(MedicalStatus.APPROVED);
            m.setApprovedBy(officer.getUserId());
            m.setApprovedAt(LocalDateTime.now().withNano(0));
        }
        return m;
    }

    @Override
    protected void save(Medical m) throws AppException {
        if (existing == null) {
            dao.add(m);      // validates the 14-day deadline and links absences in the full date range
        } else {
            dao.save(m);
        }
    }
}
