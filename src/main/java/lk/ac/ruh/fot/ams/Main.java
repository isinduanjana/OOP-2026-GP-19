package lk.ac.ruh.fot.ams;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.ui.LoginFrame;
import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * =====================================================================
 *  ICT2132 - Object Oriented Programming Practicum - Mini Project
 *  Faculty of Technology Academic Management System
 *  University of Ruhuna
 *
 *  [COMMON PART] Program entry point.
 *
 *  Project structure (who wrote what):
 *    lk.ac.ruh.fot.ams.*             -> COMMON (config, db, dao, model, service, ui)
 *    lk.ac.ruh.fot.ams.admin         -> M1  Admin
 *    lk.ac.ruh.fot.ams.lecturer      -> M2  Lecturer
 *    lk.ac.ruh.fot.ams.techofficer   -> M3  Technical Officer
 *    lk.ac.ruh.fot.ams.undergraduate -> M4  Undergraduate
 * =====================================================================
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Last-chance handler: any unexpected error is shown instead of silently failing
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Unexpected error: " + e, "Error", JOptionPane.ERROR_MESSAGE));
        });

        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new LoginFrame().setVisible(true);
        });
    }
}
