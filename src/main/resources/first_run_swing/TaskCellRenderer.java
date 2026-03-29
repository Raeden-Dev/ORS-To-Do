package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.TaskItem;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TaskCellRenderer implements ListCellRenderer<TaskItem> {
    private JPanel panel;
    private JLabel dateLabel, starLabel, workTypeLabel, textLabel, priorityLabel, checkboxLabel;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

    public TaskCellRenderer() {
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        dateLabel = new JLabel();
        starLabel = new JLabel();
        workTypeLabel = new JLabel();
        textLabel = new JLabel();
        priorityLabel = new JLabel();
        checkboxLabel = new JLabel();

        dateLabel.setForeground(Color.GRAY);
        starLabel.setForeground(new Color(255, 215, 0)); // Gold

        panel.add(dateLabel);
        panel.add(starLabel);
        panel.add(workTypeLabel);
        panel.add(textLabel);
        panel.add(priorityLabel);
        panel.add(checkboxLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TaskItem> list, TaskItem task, int index, boolean isSelected, boolean cellHasFocus) {
        // 1. Handle Colors (Selection vs Custom Hex vs Default)
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

        // 2. Populate Common Data
        dateLabel.setText("[" + task.getDateCreated().format(formatter) + "]");
        starLabel.setText(task.isFavorite() ? "[⭐]" : "[  ]");
        textLabel.setText(task.getTextContent());

        // Priority Styling
        priorityLabel.setText("[" + task.getPriority().name() + "]");
        priorityLabel.setForeground(task.getPriority() == TaskItem.Priority.HIGH ? new Color(255, 100, 100) : Color.GRAY);

        // Checkbox State (Using Unicode for visual representation)
        checkboxLabel.setText(task.isFinished() ? "[☑]" : "[☐]");

        // 3. Module-Specific Formatting
        if (task.getOriginModule() == TaskItem.OriginModule.WORK) {
            workTypeLabel.setVisible(true);
            workTypeLabel.setText("[" + (task.getWorkType().isEmpty() ? "General" : task.getWorkType()) + "]");
            // Optional: Add Start/Deadline logic here if dates are set
        } else {
            workTypeLabel.setVisible(false); // Hide for Quick To-Do
        }

        // Strikethrough for finished tasks
        if (task.isFinished()) {
            textLabel.setText("<html><s>" + task.getTextContent() + "</s></html>");
        }

        return panel;
    }
}