package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.TaskItem;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TaskCellRenderer implements ListCellRenderer<TaskItem> {
    private JPanel panel, leftPanel, rightPanel;
    private JLabel dateLabel, starLabel, workTypeLabel, textLabel, priorityLabel;
    private JCheckBox checkBox; // Changed to an actual JCheckBox
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public TaskCellRenderer() {
        panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        dateLabel = new JLabel();
        starLabel = new JLabel();
        workTypeLabel = new JLabel();
        textLabel = new JLabel();
        priorityLabel = new JLabel();

        // Initialize the real checkbox
        checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false); // Keeps it looking clean

        dateLabel.setForeground(Color.GRAY);
        starLabel.setForeground(new Color(255, 215, 0));

        leftPanel.add(starLabel);
        leftPanel.add(dateLabel);
        leftPanel.add(workTypeLabel);
        leftPanel.add(textLabel);

        rightPanel.add(priorityLabel);
        rightPanel.add(checkBox); // Add to the right panel

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TaskItem> list, TaskItem task, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            panel.setBackground(UIManager.getColor("List.selectionBackground"));
            textLabel.setForeground(UIManager.getColor("List.selectionForeground"));
        } else {
            if (task.getColorHex() != null && !task.getColorHex().isEmpty()) {
                panel.setBackground(Color.decode(task.getColorHex()));
            } else {
                panel.setBackground(list.getBackground());
            }
            textLabel.setForeground(task.isFinished() ? Color.GRAY : list.getForeground());
        }

        dateLabel.setText("[" + task.getDateCreated().format(formatter) + "]");

        starLabel.setText(task.isFavorite() ? "[⭐]" : "");
        starLabel.setVisible(task.isFavorite());

        textLabel.setText(task.getTextContent());

        priorityLabel.setText("[" + task.getPriority().name() + "]");
        priorityLabel.setForeground(task.getPriority() == TaskItem.Priority.HIGH ? new Color(255, 100, 100) : Color.GRAY);

        // Set the state of the real checkbox
        checkBox.setSelected(task.isFinished());

        if (task.getOriginModule() == OriginModule.WORK) {
            workTypeLabel.setVisible(true);
            workTypeLabel.setText("[" + (task.getWorkType().isEmpty() ? "General" : task.getWorkType()) + "]");
        } else {
            workTypeLabel.setVisible(false);
        }

        if (task.isFinished()) {
            textLabel.setText("<html><s>" + task.getTextContent() + "</s></html>");
        }

        return panel;
    }
}