package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.ui.shared.TimetablePanel;

/**
 * [M1 - ADMIN MODULE] Shows the week grid of a timetable by re-using the COMMON
 * TimetablePanel (the same view lecturers, TOs and students see) inside a dialog.
 *
 * OOP: Re-use by composition + Polymorphism: the loader passed in is a lambda
 *      (TimetablePanel.Loader interface) decided by the caller.
 */
public class WeekViewDialog extends AdminDialog {

    public WeekViewDialog(Component parent, Color accent, User user, String title, TimetablePanel.Loader loader) {
        super(parent, "Week view", title, "calendar", accent);
        TimetablePanel panel = new TimetablePanel(user, accent, title, loader);
        panel.setPreferredSize(new Dimension(1000, 610));
        setContent(panel);
        panel.refresh();
        ModernButton close = UI.secondary("Close", null);
        close.addActionListener(e -> dispose());
        addButton(close);
    }
}
