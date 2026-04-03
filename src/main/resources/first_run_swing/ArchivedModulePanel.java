package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ArchivedModulePanel extends JPanel {
    private DefaultListModel<TaskItem> listModel;
    private JList<TaskItem> taskList;
    private List<TaskItem> globalDatabase;
    private Runnable onStateChangedCallback;

    public ArchivedModulePanel(List<TaskItem> globalDatabase, Runnable onStateChangedCallback) {
        this.globalDatabase = globalDatabase;
        this.onStateChangedCallback = onStateChangedCallback;
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new ArchiveCellRenderer());

        refreshList();
        setupContextMenu();

        add(new JScrollPane(taskList), BorderLayout.CENTER);
    }

    public void refreshList() {
        listModel.clear();
        for (TaskItem task : globalDatabase) {
            if (task.isArchived()) {
                listModel.addElement(task);
            }
        }
    }

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem unarchiveItem = new JMenuItem("Unarchive");
        unarchiveItem.addActionListener(e -> {
            TaskItem task = taskList.getSelectedValue();
            if (task != null) {
                task.setArchived(false);
                // We leave the isFinished and DateCompleted metadata entirely intact as requested
                StorageManager.saveTasks(globalDatabase);
                onStateChangedCallback.run(); // Trigger UI sync across all tabs
            }
        });

        JMenuItem deleteItem = new JMenuItem("Permanently Delete");
        deleteItem.setForeground(new Color(255, 100, 100));
        deleteItem.addActionListener(e -> {
            TaskItem task = taskList.getSelectedValue();
            if (task != null) {
                globalDatabase.remove(task);
                StorageManager.saveTasks(globalDatabase);
                refreshList();
            }
        });

        contextMenu.add(unarchiveItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);

        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        taskList.setSelectedIndex(index);
                        contextMenu.show(taskList, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    // Custom Renderer for [Date-Completed] [Origin Module] [Text Content]
    private class ArchiveCellRenderer extends DefaultListCellRenderer {
        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            TaskItem task = (TaskItem) value;

            String dateStr = task.getDateCompleted() != null ? task.getDateCompleted().format(formatter) : "No Date";
            String originStr = task.getOriginModule().name();

            label.setText(String.format("[%s] [%s] %s", dateStr, originStr, task.getTextContent()));
            label.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

            // Maintain the original custom hex color if it had one
            if (task.getColorHex() != null && !isSelected) {
                label.setBackground(Color.decode(task.getColorHex()));
            } else if (!isSelected) {
                label.setBackground(list.getBackground());
            }

            return label;
        }
    }
}