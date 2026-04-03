package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskModulePanel extends JPanel {
    private JPanel listContainer;
    private OriginModule moduleType;
    private List<TaskItem> globalDatabase;
    private JTextField inputField = new JTextField();

    private final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public TaskModulePanel(OriginModule moduleType, List<TaskItem> globalDatabase) {
        this.moduleType = moduleType;
        this.globalDatabase = globalDatabase;
        setLayout(new BorderLayout());

        // --- NEW: Dynamic Panel List ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));

        // Wrap it so rows stay at the top instead of centering
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(listContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling

        // --- Input Area ---
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addBtn = new JButton("Add");
        JButton clearBtn = new JButton("Clear");

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightControls.add(addBtn);
        rightControls.add(clearBtn);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(rightControls, BorderLayout.EAST);

        addBtn.addActionListener(e -> addTask(inputField.getText()));
        inputField.addActionListener(e -> addTask(inputField.getText()));
        clearBtn.addActionListener(e -> inputField.setText(""));

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() {
        listContainer.removeAll();
        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == moduleType && !task.isArchived()) {
                listContainer.add(createTaskRow(task));
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createTaskRow(TaskItem task) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Lock height

        // Background Color
        if (task.getColorHex() != null) {
            row.setBackground(Color.decode(task.getColorHex()));
        }

        // --- WEST: Metadata ---
        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        westPanel.setOpaque(false);

        JLabel starLabel = new JLabel("[⭐]");
        starLabel.setForeground(new Color(255, 215, 0));
        starLabel.setVisible(task.isFavorite());
        westPanel.add(starLabel);

        JLabel dateLabel = new JLabel("[" + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "]");
        dateLabel.setForeground(Color.GRAY);
        westPanel.add(dateLabel);

        if (moduleType == OriginModule.WORK) {
            JLabel workTypeLabel = new JLabel("[" + (task.getWorkType().isEmpty() ? "General" : task.getWorkType()) + "]");
            westPanel.add(workTypeLabel);
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

        // 1. Priority Dropdown
        JComboBox<TaskItem.Priority> prioBox = new JComboBox<>(TaskItem.Priority.values());
        prioBox.setSelectedItem(task.getPriority());
        prioBox.setPreferredSize(new Dimension(80, 25));
        prioBox.addActionListener(e -> {
            task.setPriority((TaskItem.Priority) prioBox.getSelectedItem());
            StorageManager.saveTasks(globalDatabase);
        });

        // 2. Real Checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setSelected(task.isFinished());
        checkBox.addActionListener(e -> {
            task.setFinished(checkBox.isSelected());
            StorageManager.saveTasks(globalDatabase);
            refreshList(); // Redraw text strikethrough
        });

        eastPanel.add(prioBox);
        eastPanel.add(checkBox);

        row.add(westPanel, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        row.add(eastPanel, BorderLayout.EAST);

        // Context Menu Attach
        JPopupMenu menu = createContextMenu(task);
        MouseAdapter rightClickListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) menu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
        // Add listener to row and its non-interactive children so right click always works
        row.addMouseListener(rightClickListener);
        westPanel.addMouseListener(rightClickListener);
        textLabel.addMouseListener(rightClickListener);

        return row;
    }

    private void addTask(String text) {
        if (text.trim().isEmpty()) return;
        TaskItem newTask = new TaskItem(text.trim(), TaskItem.Priority.MED, moduleType);
        if (moduleType == OriginModule.WORK) newTask.setWorkType("Studio Dev");
        globalDatabase.add(newTask);
        refreshList();
        inputField.setText("");
        StorageManager.saveTasks(globalDatabase);
    }

    private JPopupMenu createContextMenu(TaskItem task) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem toggleFav = new JMenuItem(task.isFavorite() ? "Remove Favorite" : "Add Favorite");
        toggleFav.addActionListener(e -> { task.setFavorite(!task.isFavorite()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        JMenuItem editTask = new JMenuItem("Edit Task (Advanced)");
        editTask.addActionListener(e -> showEditDialog(task));

        JMenuItem archiveItem = new JMenuItem("Archive Task");
        archiveItem.addActionListener(e -> { task.setArchived(true); StorageManager.saveTasks(globalDatabase); refreshList(); });

        JMenu colorMenu = new JMenu("Set Background Color");
        for (String hex : DARK_PASTELS) {
            JMenuItem colorItem = new JMenuItem(" ");
            colorItem.setBackground(Color.decode(hex));
            colorItem.setOpaque(true);
            colorItem.addActionListener(e -> { task.setColorHex(hex); StorageManager.saveTasks(globalDatabase); refreshList(); });
            colorMenu.add(colorItem);
        }

        JMenuItem resetColor = new JMenuItem("Reset Color");
        resetColor.addActionListener(e -> { task.setColorHex(null); StorageManager.saveTasks(globalDatabase); refreshList(); });

        colorMenu.addSeparator();
        colorMenu.add(resetColor);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setForeground(new Color(255, 100, 100));
        deleteItem.addActionListener(e -> { globalDatabase.remove(task); StorageManager.saveTasks(globalDatabase); refreshList(); });

        menu.add(toggleFav);
        menu.add(editTask);
        menu.addSeparator();
        menu.add(colorMenu);
        menu.addSeparator();
        menu.add(archiveItem);
        menu.add(deleteItem);

        return menu;
    }

    private void showEditDialog(TaskItem task) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField textField = new JTextField(task.getTextContent());

        panel.add(new JLabel("Task Content:")); panel.add(textField);

        JTextField workTypeField = new JTextField(task.getWorkType());
        JTextField startField = new JTextField(task.getStartDate() != null ? task.getStartDate().toLocalDate().toString() : "");
        JTextField deadlineField = new JTextField(task.getDeadline() != null ? task.getDeadline().toLocalDate().toString() : "");

        if (task.getOriginModule() == OriginModule.WORK) {
            panel.add(new JLabel("Work Type:")); panel.add(workTypeField);
            panel.add(new JLabel("Start Date (YYYY-MM-DD):")); panel.add(startField);
            panel.add(new JLabel("Deadline (YYYY-MM-DD):")); panel.add(deadlineField);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            task.setTextContent(textField.getText().trim());

            if (task.getOriginModule() == OriginModule.WORK) {
                task.setWorkType(workTypeField.getText().trim());
                try {
                    if (!startField.getText().trim().isEmpty()) task.setStartDate(LocalDate.parse(startField.getText().trim()).atStartOfDay());
                    else task.setStartDate(null);

                    if (!deadlineField.getText().trim().isEmpty()) task.setDeadline(LocalDate.parse(deadlineField.getText().trim()).atStartOfDay());
                    else task.setDeadline(null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Date invalid. Please use YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        }
    }

    public String getPendingInput() { return inputField != null ? inputField.getText() : ""; }
}