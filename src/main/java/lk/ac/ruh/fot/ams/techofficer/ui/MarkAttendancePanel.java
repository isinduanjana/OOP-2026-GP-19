package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.model.AttendanceSession;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.techofficer.dao.TOAttendanceDAO;
import lk.ac.ruh.fot.ams.techofficer.model.RegisterRow;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M3 - TECHNICAL OFFICER MODULE] "Add and maintain attendance details".
 *
 *   course + component  ->  list of sessions (max 15)  ->  register of enrolled students
 *
 * The TO ticks Present / unticks for Absent and saves the whole register in one
 * transaction. Absent students who submitted a medical for that day are linked to
 * it automatically, so the medical's status (Approved / Pending / Rejected) is shown.
 *
 * Connection to common: extends BasePanel, uses CourseDAO.findCurrentSemester(),
 * StudentDAO.findByCourse() (inside TOAttendanceDAO) and the UI kit.
 * OOP: Inheritance, Encapsulation, Polymorphism (SessionDialog used through the
 * RecordEntryDialog template), Exception handling (safe(...), custom exceptions).
 */
public class MarkAttendancePanel extends BasePanel {

    private static final int COL_PRESENT = 3;
    private static final int COL_STATUS = 4;

    private final CourseDAO courseDAO = new CourseDAO();
    private final TOAttendanceDAO dao = new TOAttendanceDAO();

    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final JComboBox<Component> componentBox = new JComboBox<>();
    private final SessionMeter progress = new SessionMeter();
    private final JLabel progressLabel = UI.label("-");

    private final DataTableModel sessionModel = new DataTableModel("#", "Date", "Hours", "Present", "Absent");
    private final JTable sessionTable = UI.table(sessionModel);

    private final DataTableModel registerModel = new DataTableModel("TG No", "Name", "Type", "Present", "Status", "Medical");
    private final JTable registerTable = UI.table(registerModel);
    private final JLabel registerTitle = UI.heading("Select a session");
    private final JLabel registerInfo = UI.muted(" ");

    private final ModernButton allPresent = UI.secondary("Mark all present", "check");
    private final ModernButton allAbsent = UI.secondary("Mark all absent", "users");
    private final ModernButton saveBtn;
    private final ModernButton editBtn = UI.secondary("Edit session date", "calendar");
    private final ModernButton deleteBtn = UI.danger("Delete session", "trash");
    private final ModernButton newBtn;

    private AttendanceSession current;
    private boolean loading;
    private boolean dirty;          // user changed the register but did not save
    private int unsavedRows;        // enrolled students with no attendance row yet

    public MarkAttendancePanel(User user, Color accent) {
        super(user, accent);
        saveBtn = accentButton("Save attendance", "check");
        newBtn = accentButton("New session", "plus");

        // ---------------- top: selectors + progress
        courseBox.setPreferredSize(new Dimension(330, 36));
        componentBox.setPreferredSize(new Dimension(140, 36));
        courseBox.addActionListener(e -> onCourseChanged());
        componentBox.addActionListener(e -> {
            if (!loading) loadSessions(null);
        });
        newBtn.addActionListener(e -> newSession());
        CardPanel top = new CardPanel(new BorderLayout());
        JPanel meter = new JPanel(new BorderLayout(0, 4));
        meter.setOpaque(false);
        meter.add(progressLabel, BorderLayout.NORTH);
        meter.add(progress, BorderLayout.CENTER);
        top.add(UI.toolbar(UI.row(UI.label("Course"), courseBox, UI.label("Component"), componentBox),
                UI.rowRight(meter, newBtn)), BorderLayout.CENTER);

        // ---------------- left: sessions
        UI.setColumnWidths(sessionTable, 36, 96, 48, 60, 56);
        sessionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading) onSessionSelected();
        });
        CardPanel left = CardPanel.titled("Sessions");
        left.add(UI.scroll(sessionTable), BorderLayout.CENTER);
        left.add(UI.row(editBtn, deleteBtn), BorderLayout.SOUTH);
        left.setMinimumSize(new Dimension(330, 200));
        left.setPreferredSize(new Dimension(340, 400));
        editBtn.addActionListener(e -> editSession());
        deleteBtn.addActionListener(e -> deleteSession());

        // ---------------- right: register
        registerModel.setColumnClasses(String.class, String.class, String.class, Boolean.class, String.class, String.class);
        registerModel.setEditableColumns(COL_PRESENT);
        UI.setColumnWidths(registerTable, 122, 190, 108, 64, 84, 82);
        registerModel.addTableModelListener(e -> {
            if (loading || e.getType() != TableModelEvent.UPDATE || e.getColumn() != COL_PRESENT) return;
            for (int r = e.getFirstRow(); r <= e.getLastRow() && r < registerModel.getRowCount(); r++) {
                boolean p = Boolean.TRUE.equals(registerModel.getValueAt(r, COL_PRESENT));
                registerModel.getRows().get(r)[COL_STATUS] = p ? "Present" : "Absent";
            }
            dirty = true;
            updateInfo();
            registerTable.repaint();
        });
        allPresent.addActionListener(e -> markAll(true));
        allAbsent.addActionListener(e -> markAll(false));
        saveBtn.addActionListener(e -> saveRegister());

        CardPanel right = new CardPanel(new BorderLayout(0, 10));
        JPanel head = new JPanel(new BorderLayout(0, 2));
        head.setOpaque(false);
        head.add(registerTitle, BorderLayout.NORTH);
        head.add(registerInfo, BorderLayout.SOUTH);
        right.add(head, BorderLayout.NORTH);
        right.add(UI.scroll(registerTable), BorderLayout.CENTER);
        right.add(UI.toolbar(UI.row(allPresent, allAbsent), UI.rowRight(saveBtn)), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(14);
        split.setResizeWeight(0.3);
        split.setContinuousLayout(true);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        setRegisterEnabled(false);
    }

    @Override
    public String getTitle() {
        return "Mark Attendance";
    }

    @Override
    public String getSubtitle() {
        return "Create sessions (max " + AppConfig.SESSIONS_PER_COMPONENT + " per component, 1 session = "
                + AppConfig.HOURS_PER_SESSION + " h) and record who was present";
    }

    @Override
    public void refresh() {
        if (dirty && current != null && UI.confirm(this, "Save the unsaved attendance of " + current + " first?")) {
            saveRegister();
        }
        safe(() -> {
            Course selected = (Course) courseBox.getSelectedItem();
            loading = true;
            try {
                courseBox.removeAllItems();
                for (Course c : courseDAO.findCurrentSemester()) courseBox.addItem(c);
                if (selected != null) courseBox.setSelectedItem(selected);
            } finally {
                loading = false;
            }
            onCourseChanged();
        });
    }

    // ------------------------------------------------------------------ loading
    private void onCourseChanged() {
        if (loading) return;
        Course c = (Course) courseBox.getSelectedItem();
        Component prev = (Component) componentBox.getSelectedItem();
        loading = true;
        try {
            componentBox.removeAllItems();
            if (c != null) {
                // only the components the course really has
                if (c.hasTheory()) componentBox.addItem(Component.THEORY);
                if (c.hasPractical()) componentBox.addItem(Component.PRACTICAL);
                if (prev != null) componentBox.setSelectedItem(prev);
            }
        } finally {
            loading = false;
        }
        loadSessions(null);
    }

    /** Reloads the session list; re-selects the given session id if possible. */
    private void loadSessions(Integer selectId) {
        Course c = (Course) courseBox.getSelectedItem();
        Component comp = (Component) componentBox.getSelectedItem();
        loading = true;
        try {
            sessionModel.clear();
            if (c != null && comp != null) {
                safe(() -> {
                    List<AttendanceSession> list = dao.findSessions(c.getCourseId(), comp);
                    for (AttendanceSession s : list) {
                        sessionModel.addRow(s, s.getSessionNumber(), Format.date(s.getSessionDate()), s.getHours(),
                                s.getPresentCount(), s.getAbsentCount());
                    }
                    sessionModel.refresh();
                    int n = list.size();
                    progress.setHeld(n);
                    progressLabel.setText(n + "/" + AppConfig.SESSIONS_PER_COMPONENT + " sessions held");
                    progressLabel.setForeground(n >= AppConfig.SESSIONS_PER_COMPONENT ? Theme.SUCCESS : Theme.TEXT);
                    newBtn.setEnabled(n < AppConfig.SESSIONS_PER_COMPONENT);
                    newBtn.setToolTipText(n < AppConfig.SESSIONS_PER_COMPONENT ? "Add the next session"
                            : "All " + AppConfig.SESSIONS_PER_COMPONENT + " sessions are already recorded");
                });
            }
        } finally {
            loading = false;
        }
        // select requested session, else the latest one
        int row = -1;
        for (int i = 0; i < sessionModel.getRowCount(); i++) {
            AttendanceSession s = sessionModel.getItem(i);
            if (selectId != null && s.getSessionId() == selectId) row = i;
        }
        if (row < 0) row = sessionModel.getRowCount() - 1;
        if (row >= 0) {
            int view = sessionTable.convertRowIndexToView(row);
            sessionTable.setRowSelectionInterval(view, view);
            sessionTable.scrollRectToVisible(sessionTable.getCellRect(view, 0, true));
        } else {
            current = null;
            showRegister(null, List.of());
        }
    }

    private void onSessionSelected() {
        AttendanceSession s = sessionModel.getItem(UI.selectedModelRow(sessionTable));
        if (s == null) return;
        if (dirty && current != null && current.getSessionId() != s.getSessionId()
                && UI.confirm(this, "You have unsaved changes for " + current + ".\nSave them now?")) {
            saveRegister();
        }
        current = s;
        safe(() -> showRegister(s, dao.findRegister(s)));
    }

    private void showRegister(AttendanceSession s, List<RegisterRow> rows) {
        loading = true;
        try {
            registerModel.clear();
            int unsaved = 0;
            for (RegisterRow r : rows) {
                // a student without a saved row starts as "present" (most students attend)
                boolean present = r.isMarked() ? r.getPresent() : true;
                if (!r.isMarked()) unsaved++;
                String status = r.isMarked() ? (present ? "Present" : "Absent") : "Not saved";
                String medical = r.getMedicalToShow() == null ? "-" : capital(r.getMedicalToShow().name());
                registerModel.addRow(r, r.getTgNo(), r.getStudentName(), r.getStudentType().toString(),
                        present, status, medical);
            }
            registerModel.refresh();
            unsavedRows = unsaved;
            dirty = false;
        } finally {
            loading = false;
        }
        if (s == null) {
            registerTitle.setText("No session selected");
            registerInfo.setText("Create a session with \"New session\" or pick one on the left.");
        } else {
            registerTitle.setText(s.getCourseCode() + "  -  " + s.getComponent() + " session #" + s.getSessionNumber()
                    + "  (" + Format.date(s.getSessionDate()) + ", " + s.getHours() + " h)");
            updateInfo();
        }
        setRegisterEnabled(s != null);
    }

    private void updateInfo() {
        int present = 0;
        for (int r = 0; r < registerModel.getRowCount(); r++) {
            if (Boolean.TRUE.equals(registerModel.getValueAt(r, COL_PRESENT))) present++;
        }
        int total = registerModel.getRowCount();
        String note = dirty ? "   -   unsaved changes, click \"Save attendance\""
                : unsavedRows > 0 ? "   -   " + unsavedRows + " student(s) not saved yet (shown as present)" : "";
        registerInfo.setText(total + " enrolled  |  " + present + " present  |  " + (total - present) + " absent" + note);
        registerInfo.setForeground(dirty || unsavedRows > 0 ? Theme.WARNING : Theme.TEXT_MUTED);
    }

    private void setRegisterEnabled(boolean on) {
        for (ModernButton b : new ModernButton[]{allPresent, allAbsent, saveBtn, editBtn, deleteBtn}) b.setEnabled(on);
        registerTable.setEnabled(on);
    }

    // ------------------------------------------------------------------ actions
    private void markAll(boolean present) {
        loading = true;
        try {
            for (int r = 0; r < registerModel.getRowCount(); r++) {
                registerModel.getRows().get(r)[COL_PRESENT] = present;
                registerModel.getRows().get(r)[COL_STATUS] = present ? "Present" : "Absent";
            }
            registerModel.refresh();
        } finally {
            loading = false;
        }
        dirty = true;
        updateInfo();
    }

    private void saveRegister() {
        if (current == null) return;
        if (registerTable.isEditing()) registerTable.getCellEditor().stopCellEditing();
        Map<Integer, Boolean> map = new LinkedHashMap<>();
        for (int r = 0; r < registerModel.getRowCount(); r++) {
            RegisterRow row = registerModel.getItem(r);
            map.put(row.getStudentId(), Boolean.TRUE.equals(registerModel.getValueAt(r, COL_PRESENT)));
        }
        if (map.isEmpty()) {
            UI.warn(this, "No students are enrolled in this course.");
            return;
        }
        AttendanceSession saved = current;
        if (safe(() -> dao.saveRegister(saved.getSessionId(), map))) {
            dirty = false;
            loadSessions(saved.getSessionId());
            UI.info(this, "Attendance saved for " + map.size() + " students.\n"
                    + "Absences with a medical for " + Format.date(saved.getSessionDate()) + " were linked automatically.");
        }
    }

    private void newSession() {
        Course c = (Course) courseBox.getSelectedItem();
        Component comp = (Component) componentBox.getSelectedItem();
        if (c == null || comp == null) {
            UI.warn(this, "Choose a course and component first.");
            return;
        }
        int[] next = new int[1];
        if (!safe(() -> next[0] = dao.nextSessionNumber(c, comp))) return;   // SessionLimitExceededException
        AttendanceSession s = new SessionDialog(this, accent, dao, c, comp, next[0]).open();
        if (s != null) {
            dirty = false;
            loadSessions(s.getSessionId());
        }
    }

    private void editSession() {
        Course c = (Course) courseBox.getSelectedItem();
        if (current == null || c == null) return;
        AttendanceSession s = new SessionDialog(this, accent, dao, c, current).open();
        if (s != null) loadSessions(s.getSessionId());
    }

    private void deleteSession() {
        if (current == null) return;
        if (!UI.confirm(this, "Delete " + current.getCourseCode() + " " + current + "?\n"
                + "All attendance marked for this session (" + (current.getPresentCount() + current.getAbsentCount())
                + " rows) will also be deleted.")) {
            return;
        }
        int id = current.getSessionId();
        if (safe(() -> dao.deleteSession(id))) {
            current = null;
            dirty = false;
            loadSessions(null);
        }
    }

    private static String capital(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    /** Small painted meter: 15 segments, one per planned session (inner class = encapsulation). */
    private final class SessionMeter extends javax.swing.JComponent {
        private int held;

        SessionMeter() {
            setPreferredSize(new Dimension(AppConfig.SESSIONS_PER_COMPONENT * 11, 10));
        }

        void setHeld(int held) {
            this.held = held;
            setToolTipText(held + " of " + AppConfig.SESSIONS_PER_COMPONENT + " sessions recorded");
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g0) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int n = AppConfig.SESSIONS_PER_COMPONENT;
            double w = getWidth() / (double) n;
            for (int i = 0; i < n; i++) {
                g.setColor(i < held ? (held >= n ? Theme.SUCCESS : accent) : Theme.tint(accent, 0.85));
                g.fillRoundRect((int) (i * w), 1, (int) w - 3, getHeight() - 2, 4, 4);
            }
            g.dispose();
        }
    }
}
