package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DailyModuleFX extends BorderPane {
    private VBox listContainer;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private TextField inputField, prefixField;

    private final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public DailyModuleFX(List<TaskItem> globalDatabase, AppStats appStats) {
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        setPadding(new Insets(15));

        // --- TOP: Header ---
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        Label streakLabel = new Label("🔥 " + appStats.getCurrentStreak() + " Day Streak");
        streakLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF8C00;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Live Reset Countdown Timer
        Label countdownLabel = new Label();
        countdownLabel.setStyle("-fx-text-fill: #858585; -fx-font-family: 'Consolas', monospace; -fx-font-size: 14px; -fx-padding: 0 15 0 0;");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            java.time.Duration duration = java.time.Duration.between(LocalTime.now(), LocalTime.MAX);
            countdownLabel.setText(String.format("Resets in: %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        Button historyBtn = new Button("Export History");
        historyBtn.getStyleClass().add("action-btn");
        historyBtn.setOnAction(e -> exportProfessionalReport());

        headerBox.getChildren().addAll(streakLabel, spacer, countdownLabel, historyBtn);

        // --- CENTER: Dynamic List ---
        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        // --- BOTTOM: Input Area ---
        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setPadding(new Insets(15, 0, 0, 0));

        prefixField = new TextField();
        prefixField.setPromptText("[GYM]");
        prefixField.setPrefWidth(80);
        prefixField.getStyleClass().add("input-field");

        inputField = new TextField();
        inputField.setPromptText("Enter daily task...");
        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-btn");

        inputPanel.getChildren().addAll(prefixField, inputField, addBtn);

        addBtn.setOnAction(e -> addTask());
        inputField.setOnAction(e -> addTask());

        setTop(headerBox);
        setCenter(scrollPane);
        setBottom(inputPanel);
        refreshList();
    }

    public void refreshList() {
        listContainer.getChildren().clear();
        for (TaskItem task : globalDatabase) {
            if (task.getOriginModule() == TaskItem.OriginModule.DAILY && !task.isArchived()) {
                listContainer.getChildren().add(createTaskRow(task));
            }
        }
    }

    private HBox createTaskRow(TaskItem task) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("task-row");

        if (task.getColorHex() != null) row.setStyle("-fx-background-color: " + task.getColorHex() + ";");

        Label starLabel = new Label("[⭐]");
        starLabel.getStyleClass().add("task-star");
        starLabel.setVisible(task.isFavorite());
        starLabel.setManaged(task.isFavorite());

        HBox metaBox = new HBox(5, starLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (task.getPrefix() != null && !task.getPrefix().isEmpty()) {
            Label prefixLabel = new Label(task.getPrefix());
            prefixLabel.getStyleClass().add("task-prefix");
            metaBox.getChildren().add(prefixLabel);
        }

        // Exact Strikethrough Logic
        Label textLabel = new Label(task.getTextContent());
        textLabel.setWrapText(true);
        if (task.isFinished()) {
            textLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: #E0E0E0;");
        } else {
            textLabel.setStyle("-fx-strikethrough: false; -fx-text-fill: #E0E0E0;");
        }

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isFinished());
        checkBox.setOnAction(e -> {
            task.setFinished(checkBox.isSelected());
            StorageManager.saveTasks(globalDatabase);
            refreshList();
        });

        row.getChildren().addAll(metaBox, textContainer, checkBox);
        attachContextMenu(row, task);
        return row;
    }

    private void attachContextMenu(HBox row, TaskItem task) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem toggleFav = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Add Favorite");
        toggleFav.setOnAction(e -> { task.setFavorite(!task.isFavorite()); StorageManager.saveTasks(globalDatabase); refreshList(); });

        MenuItem editItem = new MenuItem(appStats.getEditMenuText());
        editItem.setOnAction(e -> showEditDialog(task));

        // --- Restored Background Color Menu ---
        Menu colorMenu = new Menu("Set Background Color");
        for (String hex : DARK_PASTELS) {
            MenuItem colorItem = new MenuItem("");
            Rectangle colorIcon = new Rectangle(14, 14, Color.web(hex));
            colorIcon.setStroke(Color.BLACK);
            colorItem.setGraphic(colorIcon);
            colorItem.setOnAction(e -> { task.setColorHex(hex); StorageManager.saveTasks(globalDatabase); refreshList(); });
            colorMenu.getItems().add(colorItem);
        }
        MenuItem resetColor = new MenuItem("Reset Background");
        resetColor.setOnAction(e -> { task.setColorHex(null); StorageManager.saveTasks(globalDatabase); refreshList(); });
        colorMenu.getItems().addAll(new SeparatorMenuItem(), resetColor);

        MenuItem deleteItem = new MenuItem(appStats.getDeleteMenuText());
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> { globalDatabase.remove(task); StorageManager.saveTasks(globalDatabase); refreshList(); });

        contextMenu.getItems().addAll(toggleFav, editItem, colorMenu, new SeparatorMenuItem(), deleteItem);
        row.setOnContextMenuRequested(e -> contextMenu.show(row, e.getScreenX(), e.getScreenY()));
    }

    private void showEditDialog(TaskItem task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Daily Task");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField contentField = new TextField(task.getTextContent());
        TextField prefixEditField = new TextField(task.getPrefix());

        grid.add(new Label("Prefix:"), 0, 0); grid.add(prefixEditField, 1, 0);
        grid.add(new Label("Content:"), 0, 1); grid.add(contentField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                task.setTextContent(contentField.getText().trim());
                task.setPrefix(prefixEditField.getText().trim());
                StorageManager.saveTasks(globalDatabase);
                refreshList();
            }
        });
    }

    private void exportProfessionalReport() {
        try {
            File exportFile = new File(System.getProperty("user.home") + "/Desktop/Daily_Productivity_Report.html");
            FileWriter writer = new FileWriter(exportFile);

            StringBuilder dates = new StringBuilder();
            StringBuilder completionRates = new StringBuilder();
            StringBuilder totalTasks = new StringBuilder();
            StringBuilder completedTasks = new StringBuilder();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            Map<LocalDate, int[]> history = appStats.getAdvancedHistoryLog();

            for (Map.Entry<LocalDate, int[]> entry : history.entrySet()) {
                dates.append("'").append(entry.getKey().format(formatter)).append("',");
                int[] stats = entry.getValue();
                double percent = stats[0] == 0 ? 0 : ((double)stats[1] / stats[0]) * 100;

                completionRates.append(String.format("%.1f", percent)).append(",");
                totalTasks.append(stats[0]).append(",");
                completedTasks.append(stats[1]).append(",");
            }

            String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ORS Productivity Report</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { background-color: #1E1E1E; color: #E0E0E0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 40px; margin: 0; }
                    .header { text-align: center; margin-bottom: 40px; border-bottom: 2px solid #3E3E42; padding-bottom: 20px; }
                    h1 { color: #4EC9B0; margin: 0; font-size: 36px; }
                    .stats-container { display: flex; justify-content: space-around; margin-bottom: 40px; }
                    /* FIXED: Changed 30% to 30%% to escape it for Java String formatting */
                    .stat-box { background-color: #2D2D30; padding: 20px; border-radius: 10px; width: 30%%; text-align: center; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border: 1px solid #3E3E42; }
                    .stat-box h3 { margin: 0 0 10px 0; color: #AAAAAA; font-size: 16px; }
                    .stat-box p { margin: 0; font-size: 32px; font-weight: bold; color: #FF8C00; }
                    .chart-container { background-color: #2D2D30; padding: 30px; border-radius: 10px; margin-bottom: 40px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border: 1px solid #3E3E42;}
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Daily Productivity Dashboard</h1>
                    <p style="color: #AAAAAA;">Generated on %s</p>
                </div>
                
                <div class="stats-container">
                    <div class="stat-box">
                        <h3>Current Active Streak</h3>
                        <p>🔥 %d Days</p>
                    </div>
                    <div class="stat-box">
                        <h3>Total Tracked Days</h3>
                        <p>%d Days</p>
                    </div>
                </div>

                <div class="chart-container">
                    <canvas id="completionChart" height="80"></canvas>
                </div>
                
                <div class="chart-container">
                    <canvas id="volumeChart" height="80"></canvas>
                </div>

                <script>
                    Chart.defaults.color = '#AAAAAA';
                    Chart.defaults.font.family = "'Segoe UI', sans-serif";

                    const dates = [%s];

                    // Completion Percentage Line Chart
                    new Chart(document.getElementById('completionChart').getContext('2d'), {
                        type: 'line',
                        data: {
                            labels: dates,
                            datasets: [{
                                label: 'Completion Rate (%%)',
                                data: [%s],
                                borderColor: '#4EC9B0',
                                backgroundColor: 'rgba(78, 201, 176, 0.2)',
                                borderWidth: 3,
                                fill: true,
                                tension: 0.3,
                                pointBackgroundColor: '#4EC9B0',
                                pointRadius: 5
                            }]
                        },
                        options: {
                            responsive: true,
                            plugins: { title: { display: true, text: 'Daily Task Completion Rate', color: '#FFFFFF', font: { size: 18 } } },
                            scales: { y: { beginAtZero: true, max: 100, grid: { color: '#3E3E42' } }, x: { grid: { color: '#3E3E42' } } }
                        }
                    });

                    // Task Volume Bar Chart
                    new Chart(document.getElementById('volumeChart').getContext('2d'), {
                        type: 'bar',
                        data: {
                            labels: dates,
                            datasets: [
                                { label: 'Tasks Completed', data: [%s], backgroundColor: '#FF8C00', borderRadius: 4 },
                                { label: 'Total Tasks Assigned', data: [%s], backgroundColor: '#569CD6', borderRadius: 4 }
                            ]
                        },
                        options: {
                            responsive: true,
                            plugins: { title: { display: true, text: 'Task Volume (Completed vs Assigned)', color: '#FFFFFF', font: { size: 18 } } },
                            scales: { y: { beginAtZero: true, grid: { color: '#3E3E42' } }, x: { grid: { color: '#3E3E42' } } }
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    appStats.getCurrentStreak(),
                    history.size(),
                    dates.toString(),
                    completionRates.toString(),
                    completedTasks.toString(),
                    totalTasks.toString()
            );

            writer.write(htmlContent);
            writer.flush();
            writer.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Beautiful Report Exported to Desktop:\nDaily_Productivity_Report.html\n\nDouble click it to open in your browser!");
            alert.setHeaderText("Export Successful");
            alert.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to export report.");
            error.show();
        }
    }

    private void addTask() {
        String text = inputField.getText().trim();
        String prefix = prefixField.getText().trim();
        if (text.isEmpty()) return;

        TaskItem.CustomPriority defaultPrio = appStats.getCustomPriorities().isEmpty() ? null : appStats.getCustomPriorities().get(0);
        TaskItem newTask = new TaskItem(text, defaultPrio, TaskItem.OriginModule.DAILY);

        if (!prefix.isEmpty()) {
            String cleanPrefix = prefix.toUpperCase();
            if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
            if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
            newTask.setPrefix(cleanPrefix);
        }

        globalDatabase.add(newTask);
        refreshList();
        inputField.clear();
        StorageManager.saveTasks(globalDatabase);
    }
}