package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.lecturer.InvalidDateRangeException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.CourseMaterial;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M2 - LECTURER MODULE] Modal dialog to add a new course material or edit one
 * (title, the date range it covers, and/or replace the file). The chosen file is
 * copied with the common FileUtil.copyToUploads(file, FileUtil.MATERIALS).
 *
 * "From" / "To" = the period of the course the material belongs to
 * (e.g. Lecture 02 covers 2026-07-13 to 2026-07-17). Stored in
 * course_materials.covers_from / covers_to.
 *
 * OOP: Inheritance (extends JDialog), Encapsulation (the result is read through
 * showDialog()), Exception handling (ValidationException / InvalidDateRangeException
 * / AppException shown in a dialog).
 */
public class MaterialDialog extends JDialog {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final JTextField titleField = UI.textField(28);
    private final JLabel fileLabel = UI.muted("No file chosen");
    private final JSpinner fromSpinner = dateSpinner();
    private final JSpinner toSpinner = dateSpinner();
    private final JLabel daysLabel = UI.muted("");
    private final CourseMaterial material;
    private File chosenFile;
    private boolean saved;

    /** @param existing null to add a new material */
    public MaterialDialog(Component parent, Course course, CourseMaterial existing, int lecturerId) {
        super(SwingUtilities.getWindowAncestor(parent), existing == null ? "Add material" : "Edit material",
                ModalityType.APPLICATION_MODAL);
        this.material = existing == null ? new CourseMaterial() : existing;
        if (existing == null) {
            material.setCourseId(course.getCourseId());
            material.setLecturerId(lecturerId);
        } else {
            titleField.setText(existing.getTitle());
            fileLabel.setText(shortName(existing.getFilePath()));
        }
        // default range for a new material (or an old one without dates): this week, Monday to Friday
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        setDate(fromSpinner, existing != null && existing.getCoversFrom() != null ? existing.getCoversFrom() : monday);
        setDate(toSpinner, existing != null && existing.getCoversTo() != null ? existing.getCoversTo() : monday.plusDays(4));
        fromSpinner.addChangeListener(e -> onFromChanged());
        toSpinner.addChangeListener(e -> updateDaysLabel());
        updateDaysLabel();

        ModernButton browse = UI.secondary("Choose file...", "upload");
        browse.addActionListener(e -> chooseFile());
        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setOpaque(false);
        fileRow.add(browse, BorderLayout.WEST);
        fileRow.add(fileLabel, BorderLayout.CENTER);

        // "From [date]  to  [date]   (5 days)"
        JPanel rangeRow = new JPanel();
        rangeRow.setLayout(new BoxLayout(rangeRow, BoxLayout.X_AXIS));
        rangeRow.setOpaque(false);
        rangeRow.add(UI.muted("From "));
        rangeRow.add(Box.createHorizontalStrut(6));
        rangeRow.add(fromSpinner);
        rangeRow.add(Box.createHorizontalStrut(10));
        rangeRow.add(UI.muted("To "));
        rangeRow.add(Box.createHorizontalStrut(6));
        rangeRow.add(toSpinner);
        rangeRow.add(Box.createHorizontalStrut(10));
        rangeRow.add(daysLabel);
        rangeRow.add(Box.createHorizontalGlue());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.add(UI.heading((existing == null ? "New material for " : "Edit material of ") + course.getCourseCode()),
                BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(UI.form(new String[]{"Title", "Covers", "File"}, titleField, rangeRow, fileRow),
                BorderLayout.CENTER);
        center.add(UI.muted("Covers = the period of the course this material belongs to (e.g. one week of lectures)."),
                BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        ModernButton cancel = UI.secondary("Cancel", null);
        cancel.addActionListener(e -> dispose());
        ModernButton ok = UI.primary(existing == null ? "Add material" : "Save changes", "check");
        ok.setBaseColor(Theme.LECTURER_ACCENT);
        ok.addActionListener(e -> save());
        root.add(UI.rowRight(cancel, ok), BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(ok);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose lecture notes, slides, lab sheet ...");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chosenFile = fc.getSelectedFile();
            fileLabel.setText(chosenFile.getName());
            if (titleField.getText().isBlank()) {
                String n = chosenFile.getName();
                titleField.setText(n.contains(".") ? n.substring(0, n.lastIndexOf('.')) : n);
            }
        }
    }

    private void save() {
        try {
            String title = Validator.required(titleField.getText(), "Title");
            if (title.length() > 100) throw new ValidationException("Title", "Title can have at most 100 characters.");
            LocalDate from = getDate(fromSpinner);
            LocalDate to = getDate(toSpinner);
            if (from == null || to == null) {
                throw new ValidationException("Date range", "Please choose both the From and To dates.");
            }
            if (to.isBefore(from)) {
                throw new InvalidDateRangeException(from, to);          // custom M2 exception
            }
            if (chosenFile == null && material.getFilePath() == null) {
                throw new ValidationException("File", "Please choose the file to upload.");
            }
            material.setTitle(title);
            material.setCoversFrom(from);
            material.setCoversTo(to);
            if (chosenFile != null) {
                material.setFilePath(FileUtil.copyToUploads(chosenFile, FileUtil.MATERIALS));
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UI.error(this, ex);
        }
    }

    /** Shows the dialog; @return the filled material, or null if cancelled. */
    public CourseMaterial showDialog() {
        setVisible(true);
        return saved ? material : null;
    }

    // ------------------------------------------------------------ date range helpers

    /** A date picker: spinner showing yyyy-MM-dd (arrow keys / typing change the day). */
    private static JSpinner dateSpinner() {
        JSpinner s = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, DATE_PATTERN));
        s.setPreferredSize(new Dimension(140, 36));
        s.setToolTipText("Type a date (" + DATE_PATTERN + ") or use the arrows");
        return s;
    }

    private static void setDate(JSpinner s, LocalDate d) {
        s.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private static LocalDate getDate(JSpinner s) {
        try {
            s.commitEdit();               // accept a date the user typed but did not confirm with Enter
        } catch (ParseException ignored) {
            // keep the last valid value
        }
        Object v = s.getValue();
        return v instanceof Date d ? d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    /** If "From" moves past "To", move "To" along so the range stays valid. */
    private void onFromChanged() {
        LocalDate from = getDate(fromSpinner);
        LocalDate to = getDate(toSpinner);
        if (from != null && to != null && to.isBefore(from)) {
            setDate(toSpinner, from);
        }
        updateDaysLabel();
    }

    private void updateDaysLabel() {
        LocalDate from = getDate(fromSpinner);
        LocalDate to = getDate(toSpinner);
        if (from == null || to == null) {
            daysLabel.setText("");
        } else if (to.isBefore(from)) {
            daysLabel.setText("'To' is before 'From'");
            daysLabel.setForeground(Theme.DANGER);
        } else {
            long days = ChronoUnit.DAYS.between(from, to) + 1;
            daysLabel.setText("(" + days + (days == 1 ? " day)" : " days)"));
            daysLabel.setForeground(Theme.TEXT_MUTED);
        }
    }

    static String shortName(String path) {
        if (path == null || path.isBlank()) return "-";
        String n = path.replace('\\', '/');
        return n.substring(n.lastIndexOf('/') + 1);
    }
}
