package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.report.AttendanceRecord;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.ComponentAttendance;
import lk.ac.ruh.fot.ams.service.AttendanceService;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ProgressRing;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Individual attendance lookup: choose a student
 * -> one card per course (theory / practical / combined %, with and without
 * approved medicals, 80% eligibility badge) -> click a card to see the
 * day-by-day register of that course.
 *
 * Connection to common: extends BasePanel; all numbers come from the common
 * AttendanceService (getStudentSummaries / getRecords) and the report models.
 * OOP: Inheritance, Encapsulation (inner CourseCard class), Composition.
 */
public class StudentAttendancePanel extends BasePanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceService service = new AttendanceService();

    private final JTextField search = UI.searchField("Filter by TG no or name");
    private final JComboBox<Undergraduate> studentBox = new JComboBox<>();
    private final JLabel studentInfo = UI.muted(" ");
    private final JPanel cards = new JPanel(new GridLayout(0, 3, 14, 14));
    private final DataTableModel recordModel = new DataTableModel("Component", "Session", "Date", "Status");
    private final JTable recordTable = UI.table(recordModel);
    private final JLabel recordTitle = UI.heading("Day-by-day attendance");
    private final List<CourseCard> courseCards = new ArrayList<>();
    private List<Undergraduate> students = new ArrayList<>();
    private boolean loading;

    public StudentAttendancePanel(User user, Color accent) {
        super(user, accent);
        studentBox.setPreferredSize(new Dimension(340, 36));
        search.setPreferredSize(new Dimension(220, 36));
        studentBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                   boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Undergraduate u) setText(u.getTgNo() + "  -  " + u.getFullName());
                return this;
            }
        });
        studentBox.addActionListener(e -> {
            if (!loading) loadStudent();
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterStudents(); }
            @Override public void removeUpdate(DocumentEvent e) { filterStudents(); }
            @Override public void changedUpdate(DocumentEvent e) { filterStudents(); }
        });

        CardPanel top = new CardPanel(new BorderLayout(0, 6));
        top.add(UI.toolbar(UI.row(UI.label("Student"), search, studentBox), null), BorderLayout.NORTH);
        top.add(studentInfo, BorderLayout.SOUTH);

        cards.setOpaque(false);

        CardPanel records = new CardPanel(new BorderLayout(0, 10));
        records.add(recordTitle, BorderLayout.NORTH);
        UI.setColumnWidths(recordTable, 100, 70, 110, 260);
        records.add(UI.scroll(recordTable), BorderLayout.CENTER);
        records.setPreferredSize(new Dimension(600, 300));

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(records, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Student Attendance";
    }

    @Override
    public String getSubtitle() {
        return "Individual summary: theory, practical and combined, with and without approved medicals";
    }

    @Override
    public void refresh() {
        safe(() -> {
            students = studentDAO.findAll();
            filterStudents();
        });
    }

    private void filterStudents() {
        String q = search.getText().trim().toLowerCase();
        Undergraduate prev = (Undergraduate) studentBox.getSelectedItem();
        DefaultComboBoxModel<Undergraduate> m = new DefaultComboBoxModel<>();
        for (Undergraduate u : students) {
            if (q.isEmpty() || u.getTgNo().toLowerCase().contains(q) || u.getFullName().toLowerCase().contains(q)) {
                m.addElement(u);
            }
        }
        loading = true;
        try {
            studentBox.setModel(m);
            if (prev != null) {
                for (int i = 0; i < m.getSize(); i++) {
                    if (m.getElementAt(i).getStudentId() == prev.getStudentId()) studentBox.setSelectedIndex(i);
                }
            }
        } finally {
            loading = false;
        }
        loadStudent();
    }

    private void loadStudent() {
        Undergraduate u = (Undergraduate) studentBox.getSelectedItem();
        cards.removeAll();
        courseCards.clear();
        recordModel.clear();
        if (u == null) {
            studentInfo.setText("No student matches the search.");
            recordTitle.setText("Day-by-day attendance");
            cards.revalidate();
            cards.repaint();
            return;
        }
        studentInfo.setText(u.getTgNo() + "  |  Batch " + u.getBatch() + "  |  Level " + u.getLevel() + " Semester "
                + u.getSemester() + "  |  " + u.getStudentType() + "  |  " + Format.nullToDash(u.getDepartmentName()));
        safe(() -> {
            List<AttendanceSummary> list = service.getStudentSummaries(u);
            for (AttendanceSummary s : list) {
                CourseCard c = new CourseCard(s);
                courseCards.add(c);
                cards.add(c);
            }
            if (list.isEmpty()) {
                cards.add(UI.muted("This student is not enrolled in any course in " + AppConfig.CURRENT_ACADEMIC_YEAR + "."));
            }
            cards.revalidate();
            cards.repaint();
            if (!courseCards.isEmpty()) select(courseCards.get(0));
        });
    }

    private void select(CourseCard card) {
        for (CourseCard c : courseCards) c.setSelected(c == card);
        AttendanceSummary s = card.summary;
        recordTitle.setText("Day-by-day attendance  -  " + s.getCourse().getCourseCode() + " "
                + s.getCourse().getCourseName());
        safe(() -> {
            recordModel.clear();
            for (AttendanceRecord r : service.getRecords(s.getStudentId(), s.getCourse().getCourseId())) {
                recordModel.addRow(r, r.getComponent().toString(), "#" + r.getSessionNumber(),
                        Format.date(r.getSessionDate()), r.getDisplayStatus());
            }
            recordModel.refresh();
        });
    }

    /** Clickable summary card of one course (inner class = encapsulation). */
    private final class CourseCard extends CardPanel {
        private final AttendanceSummary summary;
        private boolean selected;

        CourseCard(AttendanceSummary s) {
            super(new BorderLayout(12, 6));
            this.summary = s;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boolean ok = s.isEligible();
            double withMed = s.getCombinedPercentage(true);

            ProgressRing ring = new ProgressRing(84);
            ring.setValue(withMed, 100, Format.percent(withMed), "combined", ok ? Theme.SUCCESS : Theme.DANGER);

            JLabel code = UI.label(s.getCourse().getCourseCode());
            JLabel name = UI.muted(s.getCourse().getCourseName());
            name.setToolTipText(s.getCourse().toString());
            JLabel badge = UI.badge(ok ? "Eligible" : "Not eligible", ok ? Theme.SUCCESS : Theme.DANGER);
            JPanel head = new JPanel(new BorderLayout(6, 0));
            head.setOpaque(false);
            head.add(code, BorderLayout.WEST);
            head.add(badge, BorderLayout.EAST);

            JPanel lines = new JPanel(new GridLayout(0, 1, 0, 2));
            lines.setOpaque(false);
            lines.add(name);
            lines.add(line("Theory", s.getTheory()));
            lines.add(line("Practical", s.getPractical()));
            lines.add(UI.muted("Combined  " + Format.percent(s.getCombinedPercentage(false)) + "  |  med "
                    + Format.percent(withMed)));
            lines.add(UI.muted("Medicals  " + s.getApprovedMedicalCount() + " approved, "
                    + s.getPendingMedicalCount() + " other"));

            JPanel text = new JPanel(new BorderLayout(0, 4));
            text.setOpaque(false);
            text.add(head, BorderLayout.NORTH);
            text.add(lines, BorderLayout.CENTER);

            add(ring, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 16));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    select(CourseCard.this);
                }
            });
        }

        private JLabel line(String label, ComponentAttendance c) {
            if (c == null) return UI.muted(label + "  -  (none)");
            JLabel l = UI.muted(label + "  " + c.getPresent() + "/" + c.getSessionsHeld() + "  "
                    + Format.percent(c.getPercentage()) + "  |  med " + Format.percent(c.getPercentageWithMedical()));
            l.setToolTipText(label + ": present " + c.getPresent() + " of " + c.getSessionsHeld() + " sessions ("
                    + c.getAttendedHours() + "/" + c.getTotalHours() + " h), approved medicals " + c.getMedicalApproved());
            if (c.getPercentageWithMedical() + 1e-9 < AppConfig.MIN_ATTENDANCE_PERCENT) l.setForeground(Theme.DANGER);
            return l;
        }

        void setSelected(boolean s) {
            selected = s;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g0) {
            super.paintComponent(g0);
            if (selected) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(accent);
                g.setStroke(new java.awt.BasicStroke(2f));
                g.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 6, 18, 18);
                g.dispose();
            }
        }
    }
}
