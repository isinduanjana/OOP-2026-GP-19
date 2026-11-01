package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Color;
import java.awt.Component;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M1 - ADMIN MODULE] Abstract "Add / Edit" dialog with Cancel + Save buttons.
 * Subclasses (UserDialog, CourseDialog, NoticeDialog, TimetableDialog, ResetPasswordDialog)
 * only build their form and implement save().
 *
 * OOP:
 *  - Abstraction + Template Method: onSave() is the fixed algorithm, save() is the variable step.
 *  - Polymorphism : CrudPanel calls showDialog() without knowing which editor it is.
 *  - Exception handling: save() throws AppException -> the dialog stays open and shows why.
 */
public abstract class AdminFormDialog extends AdminDialog {

    protected final ModernButton saveButton;
    private boolean saved;

    protected AdminFormDialog(Component parent, String title, String subtitle, String icon, Color accent) {
        super(parent, title, subtitle, icon, accent);
        ModernButton cancel = UI.secondary("Cancel", null);
        cancel.addActionListener(e -> dispose());
        saveButton = UI.primary("Save", "check");
        saveButton.setBaseColor(accent);
        saveButton.addActionListener(e -> onSave());
        addButton(cancel);
        addButton(saveButton);
        getRootPane().setDefaultButton(null);
    }

    /** Validate + store the data. Throw an AppException to keep the dialog open. */
    protected abstract void save() throws AppException;

    /** Template method: the shared part of saving. */
    private void onSave() {
        try {
            save();                 // polymorphic call -> UserDialog.save(), CourseDialog.save() ...
            saved = true;
            dispose();
        } catch (ValidationException e) {          // includes our own DuplicateUsernameException, TimetableClashException
            UI.warn(this, e.getUserMessage());
        } catch (AppException e) {                 // DatabaseException ...
            UI.error(this, e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            UI.error(this, "Unexpected error: " + e.getMessage());
        }
    }

    /** Shows the modal dialog; returns true if the user saved. */
    public boolean showDialog() {
        open();
        return saved;
    }
}
