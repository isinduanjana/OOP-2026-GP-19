package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.Icons;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Abstract modal dialog for entering ONE record
 * (Template Method pattern). The fixed algorithm is in this class:
 *
 *     open() -> buildForm()      (subclass draws its fields)
 *     Save   -> readForm()       (subclass reads + validates -> T or ValidationException)
 *            -> save(T)          (subclass writes to the DB through its DAO)
 *            -> close, result = T
 *
 * Subclasses: SessionDialog (attendance session) and MedicalDialog (medical record).
 * Errors of ANY kind (ValidationException, DuplicateMedicalException,
 * SessionLimitExceededException, DatabaseException) are handled here once.
 *
 * Connection to common: uses the common UI kit (UI.form, buttons, Theme colours).
 * OOP: Abstraction (abstract methods), Inheritance (extends JDialog; subclasses extend
 * this), Polymorphism (open() calls the subclass versions), Generics (T), Exception handling.
 *
 * @param <T> type of record the dialog produces (AttendanceSession, Medical ...)
 */
public abstract class RecordEntryDialog<T> extends JDialog {

    protected final Color accent;
    private final JLabel errorLabel = new JLabel(" ");
    private T result;

    protected RecordEntryDialog(Component parent, String title, Color accent) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
        this.accent = accent;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    // ---------------------------------------------------------------- steps for subclasses
    /** Heading text shown at the top of the dialog. */
    protected abstract String getHeading();

    /** Builds the form panel (usually with UI.form(...)). */
    protected abstract JComponent buildForm();

    /** Reads the fields into a record; throws ValidationException for bad input. */
    protected abstract T readForm() throws ValidationException;

    /** Writes the record to the database. */
    protected abstract void save(T record) throws AppException;

    /** Optional hook: icon in the header (override to change). */
    protected String getIconName() {
        return "edit";
    }

    /** Optional hook: text of the save button. */
    protected String getSaveText() {
        return "Save";
    }

    // ---------------------------------------------------------------- template method
    /** Shows the dialog and returns the saved record, or null if cancelled. (final = fixed algorithm) */
    public final T open() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JLabel head = new JLabel(getHeading(), Icons.get(getIconName(), 22, accent), JLabel.LEFT);
        head.setIconTextGap(10);
        head.setFont(Theme.FONT_HEADING);
        head.setForeground(Theme.TEXT);
        root.add(head, BorderLayout.NORTH);

        root.add(buildForm(), BorderLayout.CENTER);     // polymorphic call

        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.FONT_SMALL);
        ModernButton cancel = UI.secondary("Cancel", null);
        cancel.addActionListener(e -> dispose());
        ModernButton ok = UI.primary(getSaveText(), "check");
        ok.setBaseColor(accent);
        ok.addActionListener(e -> onSave());
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(errorLabel, BorderLayout.NORTH);
        south.add(UI.rowRight(cancel, ok), BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(ok);
        pack();
        setMinimumSize(getSize());
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setVisible(true);      // blocks (modal) until dispose()
        return result;
    }

    private void onSave() {
        try {
            T record = readForm();          // polymorphic call - step 1
            save(record);                   // polymorphic call - step 2
            result = record;
            dispose();
        } catch (ValidationException e) {
            // input problems (incl. our custom exceptions) -> red text + dialog
            errorLabel.setText(firstLine(e.getUserMessage()));
            UI.warn(this, e.getUserMessage());
        } catch (AppException e) {
            UI.error(this, e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            UI.error(this, "Unexpected error: " + e.getMessage());
        }
    }

    private static String firstLine(String s) {
        if (s == null) return " ";
        int i = s.indexOf('\n');
        return i < 0 ? s : s.substring(0, i);
    }

    // ---------------------------------------------------------------- helpers for subclasses
    /** Date spinner (yyyy-MM-dd) that cannot go past today. */
    protected static JSpinner dateSpinner(LocalDate initial) {
        Calendar max = Calendar.getInstance();
        max.set(Calendar.HOUR_OF_DAY, 23);
        max.set(Calendar.MINUTE, 59);
        SpinnerDateModel model = new SpinnerDateModel(toDate(initial == null ? LocalDate.now() : initial),
                null, max.getTime(), Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        return sp;
    }

    protected static LocalDate readDate(JSpinner sp) throws ValidationException {
        try {
            sp.commitEdit();
        } catch (java.text.ParseException e) {
            throw new ValidationException("Date", "Please enter the date as yyyy-MM-dd.");
        }
        Date d = (Date) sp.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(12 * 3600));
    }
}
