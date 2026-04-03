package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.HistoryDialog;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DailyModulePanel extends JPanel {
    private DefaultListModel<TaskItem> listModel;
    private JList<TaskItem> taskList;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private JLabel streakLabel;
    JTextField inputField = new JTextField();

    public DailyModulePanel(List<TaskItem> globalDatabase, AppStats appStats) {
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        setLayout(new BorderLayout());

        // --- 1. Header (Streak & History) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        streakLabel = new JLabel("🔥 " + appStats.getCurrentStreak() + " Day Streak");
        streakLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        streakLabel.setForeground(new Color(255, 140, 0)); // Dark orange

        JButton historyBtn = new JButton("View History");
        historyBtn.setFocusPainted(false);
        historyBtn.addActionListener(e -> showHistoryDialog());

        headerPanel.add(streakLabel, BorderLayout.WEST);
        headerPanel.add(historyBtn, BorderLayout.EAST);

        // --- 2. Center List ---
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new DailyCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        refreshList();
        setupListInteractions(); // Same interaction logic as TaskModulePanel (toggle finish)

        // --- 3. Bottom Input Area (With Prefix) ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField prefixField = new JTextField(5);
        prefixField.putClientProperty("JTextField.placeholderText", "[GYM]"); // FlatLaf placeholder feature

        JButton addBtn = new JButton("Add");

        JPanel textFieldsPanel = new JPanel(new BorderLayout(5, 0));
        textFieldsPanel.add(prefixField, BorderLayout.WEST);
        textFieldsPanel.add(inputField, BorderLayout.CENTER);

        inputPanel.add(textFieldsPanel, BorderLayout.CENTER);
        inputPanel.add(addBtn, BorderLayout.EAST);

        addBtn.addActionListener(e -> addTask(prefixField.getText(), inputField.getText()));
        inputField.addActionListener(e -> addTask(prefixField.getText(), inputField.getText()));

        // --- Assemble ---
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void refreshList() {
        listModel.clear();
        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == OriginModule.DAILY && !task.isArchived()) {
                listModel.addElement(task);
            }
        }
    }

    private void addTask(String prefix, String text) {
        if (text.trim().isEmpty()) return;

        TaskItem newTask = new TaskItem(text.trim(), TaskItem.Priority.MED, OriginModule.DAILY);
        if (!prefix.trim().isEmpty()) {
            // Ensure format like "[GYM]" or "[DIET]"
            String cleanPrefix = prefix.trim().toUpperCase();
            if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
            if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
            newTask.setPrefix(cleanPrefix);
        }

        globalDatabase.add(newTask);
        listModel.addElement(newTask);
        StorageManager.saveTasks(globalDatabase);
    }

    private void setupListInteractions() {
        // [Include the exact same MouseListener logic from TaskModulePanel here]
        // [Specifically the e.getX() > taskList.getWidth() - 60 logic to toggle the checkbox]
    }

    private void showHistoryDialog() {
        new HistoryDialog((JFrame) SwingUtilities.getWindowAncestor(this), appStats.getHistoryLog()).setVisible(true);
    }

    // --- Custom Renderer ---
    private class DailyCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            TaskItem task = (TaskItem) value;

            String star = task.isFavorite() ? "[⭐] " : "[  ] ";
            String prefix = (task.getPrefix() != null && !task.getPrefix().isEmpty()) ? task.getPrefix() + " " : "";
            String priority = " [" + task.getPriority().name() + "]";
            String check = task.isFinished() ? " [☑]" : " [☐]";

            String fullText = star + prefix + task.getTextContent() + priority + check;

            if (task.isFinished()) {
                label.setText("<html><font color='gray'><s>" + fullText + "</s></font></html>");
            } else {
                label.setText(fullText);
            }

            label.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
            if (task.getColorHex() != null && !isSelected) {
                label.setBackground(Color.decode(task.getColorHex()));
            } else if (!isSelected) {
                label.setBackground(list.getBackground());
            }

            return label;
        }
    }
    public String getPendingInput() {
        return inputField != null ? inputField.getText() : "";
    }
}