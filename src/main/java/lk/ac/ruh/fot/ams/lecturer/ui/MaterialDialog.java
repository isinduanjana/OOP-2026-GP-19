package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.CourseMaterial;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.Validator;


public class MaterialDialog extends JDialog {

    private final JTextField titleField = UI.textField(28);
    private final JLabel fileLabel = UI.muted("No file chosen");
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

        ModernButton browse = UI.secondary("Choose file...", "upload");
        browse.addActionListener(e -> chooseFile());
        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setOpaque(false);
        fileRow.add(browse, BorderLayout.WEST);
        fileRow.add(fileLabel, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.add(UI.heading((existing == null ? "New material for " : "Edit material of ") + course.getCourseCode()),
                BorderLayout.NORTH);
        root.add(UI.form(new String[]{"Title", "File"}, titleField, fileRow), BorderLayout.CENTER);

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
            if (chosenFile == null && material.getFilePath() == null) {
                throw new ValidationException("File", "Please choose the file to upload.");
            }
            material.setTitle(title);
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

    static String shortName(String path) {
        if (path == null || path.isBlank()) return "-";
        String n = path.replace('\\', '/');
        return n.substring(n.lastIndexOf('/') + 1);
    }
}
