package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.service.ResultService;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.Avatar;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.UI;


public abstract class ResultReportPanel extends LecturerPage {

    protected final ResultService resultService = new ResultService();
    protected final StudentDAO studentDAO = new StudentDAO();

    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final JRadioButton batchRadio = new JRadioButton("Whole batch", true);
    private final JRadioButton individualRadio = new JRadioButton("Individual");
    private final JComboBox<Undergraduate> studentBox = new JComboBox<>();
    private final JPanel extras = UI.rowRight();
    private final JLabel hint = UI.muted("");

    /** Subclasses put their batch / individual view in here. */
    protected final JPanel body = new JPanel(new BorderLayout(0, 14));
    private boolean loading;

    protected ResultReportPanel(User user, Color accent) {
        super(user, accent);
        ButtonGroup g = new ButtonGroup();
        g.add(batchRadio);
        g.add(individualRadio);
        batchRadio.setOpaque(false);
        individualRadio.setOpaque(false);
        courseBox.setPreferredSize(new Dimension(330, 34));
        studentBox.setPreferredSize(new Dimension(270, 34));
        studentBox.setEnabled(false);

        CardPanel filters = new CardPanel(new GridLayout(2, 1, 0, 8));
        filters.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        filters.add(UI.row(UI.label("Course"), courseBox, Box.createHorizontalStrut(10),
                UI.label("Show"), batchRadio, individualRadio, studentBox));
        filters.add(UI.toolbar(UI.row(hint), extras));
        body.setOpaque(false);

        add(filters, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        courseBox.addActionListener(e -> { if (!loading) loadStudents(); });
        studentBox.addActionListener(e -> { if (!loading && individualRadio.isSelected()) render(); });
        batchRadio.addActionListener(e -> { studentBox.setEnabled(false); render(); });
        individualRadio.addActionListener(e -> { studentBox.setEnabled(true); render(); });
    }

    // ------------------------------------------------------------ hooks for subclasses
    /** Draws the whole-batch view of a course. */
    protected abstract void renderBatch(Course course) throws AppException;

    /** Draws the view of one undergraduate. */
    protected abstract void renderIndividual(Course course, Undergraduate student) throws AppException;

    /** Lets a subclass add its own buttons / check boxes to the filter card. */
    protected void addFilter(JComponent... components) {
        for (JComponent c : components) extras.add(c);
    }

    protected void setHint(String text) {
        hint.setText(text);
    }

    protected Course selectedCourse() {
        return (Course) courseBox.getSelectedItem();
    }

    protected boolean isBatchView() {
        return batchRadio.isSelected();
    }

    // ------------------------------------------------------------ loading
    @Override
    public void refresh() {
        loading = true;
        boolean ok = safe(() -> fillCombo(courseBox, myCourses()));
        loading = false;
        if (ok) loadStudents();
    }

    private void loadStudents() {
        Course c = selectedCourse();
        loading = true;
        if (c == null) {
            studentBox.removeAllItems();
        } else {
            safe(() -> fillCombo(studentBox, studentDAO.findByCourse(c.getCourseId())));
        }
        loading = false;
        render();
    }

    /** Template method: clears the body and calls the right abstract method. */
    protected final void render() {
        body.removeAll();
        Course course = selectedCourse();
        if (course == null) {
            body.add(emptyCard("You are not assigned to any course yet."), BorderLayout.CENTER);
        } else if (isBatchView()) {
            safe(() -> renderBatch(course));
        } else {
            Undergraduate s = (Undergraduate) studentBox.getSelectedItem();
            if (s == null) {
                body.add(emptyCard("No undergraduates are enrolled in " + course.getCourseCode()
                        + " this academic year."), BorderLayout.CENTER);
            } else {
                safe(() -> renderIndividual(course, s));
            }
        }
        body.revalidate();
        body.repaint();
    }

    // ------------------------------------------------------------ shared widgets
    protected static CardPanel emptyCard(String text) {
        CardPanel c = new CardPanel(new BorderLayout());
        JLabel l = UI.muted(text);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(l, BorderLayout.CENTER);
        c.setPreferredSize(new Dimension(400, 160));
        return c;
    }

    /** Card with avatar, name, TG number, batch and type of a student + optional extra widgets on the right. */
    protected CardPanel studentHeader(Undergraduate s, String note, JComponent... right) {
        CardPanel card = new CardPanel(new BorderLayout(18, 0));
        Avatar av = new Avatar(64);
        av.setUser(s.getInitials(), s.getProfilePicture(), accent);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(UI.heading(s.getFullName()));
        text.add(Box.createVerticalStrut(4));
        text.add(UI.muted(s.getTgNo() + "   •   Batch " + s.getBatch() + "   •   Level " + s.getLevel()
                + " Semester " + s.getSemester() + "   •   " + s.getStudentType()));
        if (note != null) {
            text.add(Box.createVerticalStrut(8));
            JLabel n = UI.muted("<html><div style='width:420px'>" + note + "</div></html>");
            n.setForeground(Theme.TEXT);
            text.add(n);
        }
        JPanel avWrap = new JPanel(new BorderLayout());
        avWrap.setOpaque(false);
        avWrap.add(av, BorderLayout.NORTH);
        card.add(avWrap, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        if (right.length > 0) card.add(UI.rowRight(right), BorderLayout.EAST);
        return card;
    }
}
