package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [M1 - ADMIN MODULE] A JList whose rows are check boxes (click or SPACE toggles a row).
 * Used to assign several lecturers to a course.
 *
 * OOP: Generics (CheckBoxList&lt;T&gt; works for any item type), Inheritance (extends JList),
 *      Encapsulation (the set of checked items is private), Polymorphism (custom renderer).
 *
 * @param <T> item type
 */
public class CheckBoxList<T> extends JList<T> {

    private final DefaultListModel<T> listModel = new DefaultListModel<>();
    private final Set<T> checked = new HashSet<>();
    private final Function<T, String> textOf;
    private final List<Runnable> listeners = new ArrayList<>();

    public CheckBoxList(Function<T, String> textOf) {
        this.textOf = textOf;
        setModel(listModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new Renderer());
        setVisibleRowCount(6);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int i = locationToIndex(e.getPoint());
                if (i >= 0 && getCellBounds(i, i).contains(e.getPoint())) toggle(i);
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && getSelectedIndex() >= 0) toggle(getSelectedIndex());
            }
        });
    }

    public void setItems(List<T> items) {
        listModel.clear();
        checked.clear();
        for (T t : items) listModel.addElement(t);
    }

    public void setChecked(T item, boolean on) {
        if (on) checked.add(item); else checked.remove(item);
        repaint();
    }

    public List<T> getCheckedItems() {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            if (checked.contains(listModel.get(i))) list.add(listModel.get(i));
        }
        return list;
    }

    public void onChange(Runnable r) {
        listeners.add(r);
    }

    private void toggle(int index) {
        T item = listModel.get(index);
        setChecked(item, !checked.contains(item));
        listeners.forEach(Runnable::run);
    }

    /** Draws each row as a check box. */
    private final class Renderer implements ListCellRenderer<T> {
        private final JCheckBox box = new JCheckBox();

        @Override
        public Component getListCellRendererComponent(JList<? extends T> list, T value, int index,
                                                      boolean selected, boolean focus) {
            box.setText(textOf.apply(value));
            box.setSelected(checked.contains(value));
            box.setFont(Theme.FONT);
            box.setOpaque(true);
            box.setBackground(selected ? Theme.tint(Theme.PRIMARY, 0.9) : Theme.SURFACE);
            box.setForeground(Theme.TEXT);
            box.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            box.setBorderPainted(true);
            return box;
        }
    }
}
