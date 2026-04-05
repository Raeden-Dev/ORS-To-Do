package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DailyModulePanel extends JPanel {
    private JPanel listContainer;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private JTextField inputField = new JTextField();

    public DailyModulePanel(List<TaskItem> globalDatabase, AppStats appStats) {
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel streakLabel = new JLabel("🔥 " + appStats.getCurrentStreak() + " Day Streak");
        streakLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        streakLabel.setForeground(new Color(255, 140, 0));

        JButton historyBtn = new JButton("View History");
        historyBtn.setFocusPainted(false);
        historyBtn.addActionListener(e -> new HistoryDialog((JFrame) SwingUtilities.getWindowAncestor(this), appStats.getHistoryLog()).setVisible(true));

        headerPanel.add(streakLabel, BorderLayout.WEST);
        headerPanel.add(historyBtn, BorderLayout.EAST);

        // --- Dynamic Panel List ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(listContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- Input Area ---
        JPanel inputPanel = new JPanel(new BorderLayout(10, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField prefixField = new JTextField(6);
        prefixField.putClientProperty("JTextField.placeholderText", "[PREFIX]");

        JButton addBtn = new JButton("Add");

        JPanel leftInputs = new JPanel(new BorderLayout(5, 0));
        leftInputs.add(prefixField, BorderLayout.WEST);
        leftInputs.add(inputField, BorderLayout.CENTER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightControls.add(addBtn);

        inputPanel.add(leftInputs, BorderLayout.CENTER);
        inputPanel.add(rightControls, BorderLayout.EAST);

        addBtn.addActionListener(e -> addTask(prefixField.getText(), inputField.getText()));
        inputField.addActionListener(e -> addTask(prefixField.getText(), inputField.getText()));

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() {
        listContainer.removeAll();
        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == OriginModule.DAILY && !task.isArchived()) {
                listContainer.add(createTaskRow(task));
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createTaskRow(TaskItem task) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        if (task.getColorHex() != null) row.setBackground(Color.decode(task.getColorHex()));

        // --- WEST: Metadata ---
        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        westPanel.setOpaque(false);

        JLabel starLabel = new JLabel("[⭐]");
        starLabel.setForeground(new Color(255, 215, 0));
        starLabel.setVisible(task.isFavorite());
        westPanel.add(starLabel);

        if (task.getPrefix() != null && !task.getPrefix().isEmpty()) {
            JLabel prefixLabel = new JLabel(task.getPrefix());
            prefixLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            westPanel.add(prefixLabel);
        }

        // --- CENTER: Text ---
        JLabel textLabel = new JLabel(task.getTextContent());
        if (task.isFinished()) {
            textLabel.setText("<html><s>" + task.getTextContent() + "</s></html>");
            textLabel.setForeground(Color.GRAY);
        }

        // --- EAST: Interactive Controls ---
        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        eastPanel.setOpaque(false);

        JComboBox<TaskItem.Priority> prioBox = new JComboBox<>(TaskItem.Priority.values());
        prioBox.setSelectedItem(task.getPriority());
        prioBox.setPreferredSize(new Dimension(80, 25));
        prioBox.addActionListener(e -> {
            task.setPriority((TaskItem.Priority) prioBox.getSelectedItem());
            StorageManager.saveTasks(globalDatabase);
        });

        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setSelected(task.isFinished());
        checkBox.addActionListener(e -> {
            task.setFinished(checkBox.isSelected());
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        });

        eastPanel.add(prioBox);
        eastPanel.add(checkBox);

        row.add(westPanel, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        row.add(eastPanel, BorderLayout.EAST);

        JPopupMenu menu = createContextMenu(task);
        MouseAdapter rightClickListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) menu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
        row.addMouseListener(rightClickListener);
        westPanel.addMouseListener(rightClickListener);
        textLabel.addMouseListener(rightClickListener);

        return row;
    }

    private void addTask(String prefix, String text) {
        if (text.trim().isEmpty()) return;
        TaskItem newTask = new TaskItem(text.trim(), TaskItem.Priority.MED, OriginModule.DAILY);
        if (!prefix.trim().isEmpty()) {
            String cleanPrefix = prefix.trim().toUpperCase();
            if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
            if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
            newTask.setPrefix(cleanPrefix);
        }
        globalDatabase.add(newTask);
        refreshList();
        inputField.setText("");
        StorageManager.saveTasks(globalDatabase);
    }

    private JPopupMenu createContextMenu(TaskItem task) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem toggleFav = new JMenuItem(task.isFavorite() ? "Remove Favorite" : "Add Favorite");
        toggleFav.addActionListener(e -> { task.setFavorite(!task.isFavorite()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setForeground(new Color(255, 100, 100));
        deleteItem.addActionListener(e -> { globalDatabase.remove(task); StorageManager.saveTasks(globalDatabase); refreshList(); });

        menu.add(toggleFav);
        menu.addSeparator();
        menu.add(deleteItem);
        return menu;
    }

    public String getPendingInput() { return inputField != null ? inputField.getText() : ""; }
}