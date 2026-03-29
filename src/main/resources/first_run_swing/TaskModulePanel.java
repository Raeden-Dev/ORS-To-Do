package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TaskModulePanel extends JPanel {
    private DefaultListModel<TaskItem> listModel;
    private JList<TaskItem> taskList;
    private TaskItem.OriginModule moduleType;
    private List<TaskItem> globalDatabase;
    JTextField inputField = new JTextField();

    // 20 Dark-Theme Friendly Pastel Colors
    private final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public TaskModulePanel(TaskItem.OriginModule moduleType, List<TaskItem> globalDatabase) {
        this.moduleType = moduleType;
        this.globalDatabase = globalDatabase;
        setLayout(new BorderLayout());

        // --- 1. Setup List and Model ---
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadTasksIntoModel();

        // --- 2. Add Interactions (Click & Context Menu) ---
        setupListInteractions();

        // --- 3. Setup Bottom Input Area ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JButton addBtn = new JButton("Add");
        JButton clearBtn = new JButton("Clear");

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.add(addBtn);
        btnPanel.add(clearBtn);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);

        // Actions
        addBtn.addActionListener(e -> addTask(inputField.getText()));
        inputField.addActionListener(e -> addTask(inputField.getText())); // Enter key
        clearBtn.addActionListener(e -> inputField.setText(""));

        // --- 4. Assemble ---
        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void loadTasksIntoModel() {
        listModel.clear();
        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == moduleType) {
                listModel.addElement(task);
            }
        }
    }

    private void addTask(String text) {
        if (text.trim().isEmpty()) return;

        TaskItem newTask = new TaskItem(text.trim(), TaskItem.Priority.MED, moduleType);
        if (moduleType == TaskItem.OriginModule.WORK) {
            newTask.setWorkType("Studio Dev"); // Default example
        }

        globalDatabase.add(newTask);
        listModel.addElement(newTask);
        StorageManager.saveTasks(globalDatabase); // Save immediately
    }

    public void refreshList() {
        listModel.clear();
        for (TaskItem task : globalDatabase) {
            // Only load tasks that match this module AND are not archived
            if (task.getOriginModule() == moduleType && !task.isArchived()) {
                listModel.addElement(task);
            }
        }
    }

    private void setupListInteractions() {
        JPopupMenu contextMenu = createContextMenu();

        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = taskList.locationToIndex(e.getPoint());
                if (index == -1) return;

                taskList.setSelectedIndex(index);
                TaskItem selectedTask = listModel.getElementAt(index);

                // Right Click -> Context Menu
                if (SwingUtilities.isRightMouseButton(e)) {
                    contextMenu.show(taskList, e.getX(), e.getY());
                }
                // Left Click -> Check if clicking near the end (approximate checkbox toggle)
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Quick approximation: if click is on the far right, toggle finish
                    if (e.getX() > taskList.getWidth() - 60) {
                        selectedTask.setFinished(!selectedTask.isFinished());
                        taskList.repaint();
                        StorageManager.saveTasks(globalDatabase);
                    }
                }
            }
        });
    }

    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem toggleFav = new JMenuItem("Toggle Favorite");
        toggleFav.addActionListener(e -> {
            TaskItem task = taskList.getSelectedValue();
            if (task != null) { task.setFavorite(!task.isFavorite()); taskList.repaint(); }
        });

        JMenuItem editTask = new JMenuItem("Edit Task");
        editTask.addActionListener(e -> {
            TaskItem task = taskList.getSelectedValue();
            if (task != null) {
                String newText = JOptionPane.showInputDialog(this, "Edit Task:", task.getTextContent());
                if (newText != null && !newText.trim().isEmpty()) {
                    task.setTextContent(newText.trim());
                    taskList.repaint();
                }
            }
        });

        JMenu colorMenu = new JMenu("Set Background Color");
        for (String hex : DARK_PASTELS) {
            JMenuItem colorItem = new JMenuItem(" "); // Blank text
            colorItem.setBackground(Color.decode(hex));
            colorItem.setOpaque(true);
            colorItem.addActionListener(e -> {
                TaskItem task = taskList.getSelectedValue();
                if (task != null) { task.setColorHex(hex); taskList.repaint(); }
            });
            colorMenu.add(colorItem);
        }

        JMenuItem resetColor = new JMenuItem("Reset Color");
        resetColor.addActionListener(e -> {
            TaskItem task = taskList.getSelectedValue();
            if (task != null) { task.setColorHex(null); taskList.repaint(); }
        });
        colorMenu.addSeparator();
        colorMenu.add(resetColor);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setForeground(new Color(255, 100, 100));
        deleteItem.addActionListener(e -> {
            TaskItem task = taskList.getSelectedValue();
            if (task != null) {
                listModel.removeElement(task);
                globalDatabase.remove(task);
                StorageManager.saveTasks(globalDatabase);
            }
        });

        menu.add(toggleFav);
        menu.add(editTask);
        menu.add(colorMenu);
        menu.addSeparator();
        menu.add(deleteItem);

        return menu;
    }
    public String getPendingInput() {
        return inputField != null ? inputField.getText() : "";
    }
}