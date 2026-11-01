package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.model.report.SemesterResult;
import lk.ac.ruh.fot.ams.model.report.Transcript;
import lk.ac.ruh.fot.ams.service.ResultService;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M2 - LECTURER MODULE] "GPA (SGPA &amp; CGPA)" page - handout: "SGPA and CGPA with
 * subjects and grades for each undergraduate and for the whole batch".
 *
 *   top    : whole batch - SGPA of every semester, CGPA and class (common ResultService.getAllTranscripts)
 *   bottom : selected student's transcript (semester, course, credits, marks, grade, GP) + SGPA per semester
 *
 * The transcripts need many queries, so they are loaded in a SwingWorker (background
 * thread) while a "Loading..." label is shown - the window does not freeze.
 *
 * OOP: Inheritance (LecturerPage -> BasePanel), anonymous inner class (SwingWorker subclass),
 * Exception handling (ExecutionException unwrapped to the AppException that caused it).
 */
public class GpaPanel extends LecturerPage {

    private static final String ALL_BATCHES = "All batches";

    private final ResultService resultService = new ResultService();
    private final StudentDAO studentDAO = new StudentDAO();

    private final JComboBox<String> batchBox = new JComboBox<>();
    private final JTextField search = UI.searchField("Search student...");
    private final JLabel status = UI.muted("");
    private final JPanel batchHolder = new JPanel(new BorderLayout());
    private JTable batchTable;
    private DataTableModel batchModel;
    private List<String> semesterKeys = new ArrayList<>();

    private final DataTableModel transcriptModel = new DataTableModel("Sem", "Code", "Course", "Credits",
            "Total /100", "Grade", "GP", "Source");
    private final DataTableModel semesterModel = new DataTableModel("Semester", "Credits", "SGPA");
    private final JLabel transcriptTitle = UI.heading("Transcript");
    private final JLabel cgpaLabel = UI.muted("Select a student in the table above.");

    private final StatCard studentsCard;
    private final StatCard avgCard;
    private final StatCard firstCard;
    private final StatCard belowCard;

    private List<Transcript> transcripts = new ArrayList<>();
    private boolean loading;

    public GpaPanel(User user, Color accent) {
        super(user, accent);
        studentsCard = new StatCard("Undergraduates", "-", "users", accent);
        avgCard = new StatCard("Average CGPA", "-", "chart", Theme.INFO);
        firstCard = new StatCard("First class (≥ 3.70)", "-", "award", Theme.SUCCESS);
        belowCard = new StatCard("Below pass (< 2.00)", "-", "medical", Theme.DANGER);

        batchBox.setPreferredSize(new Dimension(160, 34));
        batchBox.addActionListener(e -> { if (!loading) renderBatch(); });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        ModernButton reload = UI.secondary("Reload", "refresh");
        reload.addActionListener(e -> load());

        batchHolder.setOpaque(false);
        CardPanel batchCard = new CardPanel(new BorderLayout(0, 12));
        batchCard.add(UI.toolbar(UI.row(UI.heading("Whole batch"), UI.label("  Batch"), batchBox,
                UI.label("Search"), search),
                UI.rowRight(status, reload)), BorderLayout.NORTH);
        batchCard.add(batchHolder, BorderLayout.CENTER);
        batchCard.setPreferredSize(new Dimension(400, 300));

        JTable tTable = UI.table(transcriptModel);

        JTable sTable = UI.table(semesterModel);
        UI.setColumnWidths(sTable, 150, 60, 60);
        JPanel semBox = new JPanel(new BorderLayout(0, 8));
        semBox.setOpaque(false);
        semBox.add(UI.label("SGPA per semester"), BorderLayout.NORTH);
        semBox.add(UI.scroll(sTable), BorderLayout.CENTER);
        semBox.setPreferredSize(new Dimension(300, 10));

        JPanel detail = new JPanel(new BorderLayout(14, 0));
        detail.setOpaque(false);
        detail.add(fitOrScroll(tTable, 60, 80, 240, 60, 75, 55, 55, 90), BorderLayout.CENTER);
        detail.add(semBox, BorderLayout.EAST);
        CardPanel detailCard = new CardPanel(new BorderLayout(0, 10));
        detailCard.add(UI.toolbar(transcriptTitle, UI.rowRight(cgpaLabel)), BorderLayout.NORTH);
        detailCard.add(detail, BorderLayout.CENTER);
        detailCard.setPreferredSize(new Dimension(400, 270));

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(batchCard, BorderLayout.CENTER);
        center.add(detailCard, BorderLayout.SOUTH);

        add(statRow(studentsCard, avgCard, firstCard, belowCard), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "GPA (SGPA & CGPA)";
    }

    @Override
    public String getSubtitle() {
        return "SGPA of every semester and CGPA with subjects and grades - whole batch and individuals";
    }

    @Override
    public void refresh() {
        safe(() -> {
            loading = true;
            List<String> batches = new ArrayList<>();
            batches.add(ALL_BATCHES);
            batches.addAll(studentDAO.findBatches());
            fillCombo(batchBox, batches);
            loading = false;
        });
        load();
    }

    /** True while the background worker is still running (used by the smoke test). */
    public boolean isLoading() {
        return status.getText().startsWith("Loading");
    }

    /** Loads all transcripts in a background thread. */
    private void load() {
        status.setText("Loading transcripts...");
        new SwingWorker<List<Transcript>, Void>() {
            @Override
            protected List<Transcript> doInBackground() throws Exception {
                return resultService.getAllTranscripts();
            }

            @Override
            protected void done() {
                try {
                    transcripts = get();
                    status.setText(transcripts.size() + " transcript(s) loaded");
                } catch (ExecutionException e) {
                    status.setText("Could not load");
                    if (e.getCause() instanceof AppException ae) UI.error(GpaPanel.this, ae);
                    else UI.error(GpaPanel.this, "Unexpected error: " + e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                renderBatch();
            }
        }.execute();
    }

    private List<Transcript> visible() {
        String b = (String) batchBox.getSelectedItem();
        List<Transcript> list = new ArrayList<>();
        for (Transcript t : transcripts) {
            if (b == null || ALL_BATCHES.equals(b) || b.equals(t.getStudent().getBatch())) list.add(t);
        }
        return list;
    }

    /** Builds the batch table; semester columns are created from the data (L1 S1, L1 S2, L2 S1 ...). */
    private void renderBatch() {
        List<Transcript> list = visible();
        TreeSet<String> keys = new TreeSet<>();
        for (Transcript t : list) {
            for (SemesterResult s : t.getSemesters()) keys.add(s.getLevel() + "-" + s.getSemester());
        }
        semesterKeys = new ArrayList<>(keys);
        List<String> cols = new ArrayList<>(List.of("TG No", "Name", "Batch", "Type"));
        for (String k : semesterKeys) cols.add("SGPA L" + k.charAt(0) + " S" + k.charAt(2));
        cols.addAll(List.of("CGPA", "Credits", "Class / standing"));
        batchModel = new DataTableModel(cols.toArray(new String[0]));

        double sum = 0;
        int counted = 0;
        int first = 0;
        int below = 0;
        for (Transcript t : list) {
            Undergraduate s = t.getStudent();
            List<Object> row = new ArrayList<>(List.of(s.getTgNo(), s.getFullName(), s.getBatch(),
                    s.getStudentType().toString()));
            for (String k : semesterKeys) row.add(sgpaText(t, k));
            boolean graded = t.getTotalCredits() > 0;
            row.add(graded ? Format.gpa(t.getCgpa()) : "-");
            row.add(t.getTotalCredits());
            row.add(graded ? t.getStanding() : "No grades yet");
            batchModel.addRow(t, row.toArray());
            if (graded) {
                sum += t.getCgpa();
                counted++;
                if (t.getCgpa() >= 3.70) first++;
                if (t.getCgpa() < 2.0) below++;
            }
        }
        batchModel.refresh();
        batchTable = UI.table(batchModel);
        int[] w = new int[cols.size()];
        java.util.Arrays.fill(w, 85);
        w[0] = 115;
        w[1] = 180;
        w[2] = 60;
        w[3] = 100;
        w[cols.size() - 1] = 170;
        batchTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showTranscript(batchModel.getItem(UI.selectedModelRow(batchTable)));
        });
        batchHolder.removeAll();
        batchHolder.add(fitOrScroll(batchTable, w), BorderLayout.CENTER);
        batchHolder.revalidate();
        batchHolder.repaint();
        filter();

        studentsCard.setValue(String.valueOf(list.size()));
        studentsCard.setCaption(String.valueOf(batchBox.getSelectedItem()));
        avgCard.setValue(counted == 0 ? "-" : Format.gpa(sum / counted));
        avgCard.setCaption("of " + counted + " graded student(s)");
        firstCard.setValue(String.valueOf(first));
        firstCard.setCaption("CGPA 3.70 or more");
        belowCard.setValue(String.valueOf(below));
        belowCard.setCaption("CGPA under 2.00");

        if (batchTable.getRowCount() > 0) batchTable.setRowSelectionInterval(0, 0);
        else showTranscript(null);
    }

    private void filter() {
        if (batchTable != null) UI.filter(batchTable, search.getText());
    }

    private static String sgpaText(Transcript t, String key) {
        for (SemesterResult s : t.getSemesters()) {
            if ((s.getLevel() + "-" + s.getSemester()).equals(key)) {
                return s.getGradedCredits() == 0 ? "Pending" : Format.gpa(s.getSgpa());
            }
        }
        return "-";
    }

    /** Fills the transcript + SGPA tables for one student. */
    private void showTranscript(Transcript t) {
        transcriptModel.clear();
        semesterModel.clear();
        if (t == null) {
            transcriptTitle.setText("Transcript");
            cgpaLabel.setText("Select a student in the table above.");
        } else {
            Undergraduate s = t.getStudent();
            transcriptTitle.setText("Transcript - " + s.getTgNo() + "  " + s.getFullName());
            for (SemesterResult sem : t.getSemesters()) {
                for (CourseResult r : sem.getResults()) {
                    transcriptModel.addRow(r, "L" + sem.getLevel() + " S" + sem.getSemester(), r.getCourse().getCourseCode(), r.getCourse().getCourseName(),
                            r.getCourse().getCredits(), Format.num(r.getTotalMarks()), r.getGradeLetter(),
                            r.hasGrade() ? Format.gpa(r.getGradePoint()) : "-", sourceText(r));
                }
                semesterModel.addRow(sem, sem.getLabel(), sem.getGradedCredits(),
                        sem.getGradedCredits() == 0 ? "Pending" : Format.gpa(sem.getSgpa()));
            }
            semesterModel.addRow(null, "CGPA (all semesters)", t.getTotalCredits(),
                    t.getTotalCredits() == 0 ? "-" : Format.gpa(t.getCgpa()));
            cgpaLabel.setText("CGPA " + (t.getTotalCredits() == 0 ? "-" : Format.gpa(t.getCgpa()))
                    + "   •   " + t.getStanding() + "   •   " + t.getTotalCredits() + " graded credits");
        }
        transcriptModel.refresh();
        semesterModel.refresh();
    }

    private static String sourceText(CourseResult r) {
        return switch (r.getSource()) {
            case PUBLISHED -> "Published";
            case PENDING -> "Pending";
            default -> r.hasGrade() ? "Live" : "In progress";
        };
    }
}
