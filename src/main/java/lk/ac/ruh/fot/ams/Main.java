package lk.ac.ruh.fot.ams;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.ui.LoginFrame;
import lk.ac.ruh.fot.ams.ui.Theme;


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
