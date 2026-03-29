package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class TaskTrackerFXApp extends Application {

    private List<TaskItem> taskDatabase;
    private AppStats appStats;
    private BorderPane rootLayout;
    private VBox sidebar;

    private TaskModuleFX quickToDoPanel;
    private DailyModuleFX dailyToDoPanel;
    private TaskModuleFX workListPanel;
    private FocusHubModuleFX focusHubPanel;
    private ArchivedModuleFX archivedPanel;
    private SettingsModuleFX settingsPanel;

    @Override
    public void init() throws Exception {
        taskDatabase = StorageManager.loadTasks();
        appStats = StorageManager.loadStats();
        processDailyRollover();
    }

    private void processDailyRollover() {
        LocalDate today = LocalDate.now();
        LocalDate lastOpened = appStats.getLastOpenedDate();

        if (today.isAfter(lastOpened)) {
            int totalDaily = 0;
            int completedDaily = 0;

            for (TaskItem task : taskDatabase) {
                if (task.getOriginModule() == TaskItem.OriginModule.DAILY && !task.isArchived()) {
                    totalDaily++;
                    if (task.isFinished()) completedDaily++;
                }
            }

            if (totalDaily > 0) {
                double percentComplete = (double) completedDaily / totalDaily;
                appStats.addHistoryRecord(lastOpened, percentComplete);
                appStats.getAdvancedHistoryLog().put(lastOpened, new int[]{totalDaily, completedDaily});

                if (percentComplete >= 1.0) appStats.setCurrentStreak(appStats.getCurrentStreak() + 1);
                else appStats.setCurrentStreak(0);
            }

            // Reset Daily Tasks
            for (TaskItem task : taskDatabase) {
                if (task.getOriginModule() == TaskItem.OriginModule.DAILY && !task.isArchived()) {
                    task.setFinished(false);
                }
            }
            appStats.setLastOpenedDate(today);
            StorageManager.saveStats(appStats);
            StorageManager.saveTasks(taskDatabase);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        rootLayout = new BorderPane();

        Runnable syncUI = () -> {
            if (quickToDoPanel != null) quickToDoPanel.refreshList();
            if (workListPanel != null) workListPanel.refreshList();
            if (dailyToDoPanel != null) dailyToDoPanel.refreshList();
        };

        quickToDoPanel = new TaskModuleFX(TaskItem.OriginModule.QUICK, taskDatabase, appStats);
        dailyToDoPanel = new DailyModuleFX(taskDatabase, appStats);
        workListPanel = new TaskModuleFX(TaskItem.OriginModule.WORK, taskDatabase, appStats);
        focusHubPanel = new FocusHubModuleFX(appStats);
        archivedPanel = new ArchivedModuleFX(taskDatabase, syncUI);
        settingsPanel = new SettingsModuleFX(appStats, taskDatabase, syncUI);

        setupSidebar();

        Scene scene = new Scene(rootLayout, 1200, 800);
        String css = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Task-Tracker (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (!sidebar.getChildren().isEmpty() && sidebar.getChildren().get(0) instanceof Button) {
            Button firstBtn = (Button) sidebar.getChildren().get(0);
            setActiveButton(firstBtn);
            switchModule("Quick To-Do");
        }
    }

    private void setupSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        String[] modules = {"Quick To-Do", "Daily To-Do", "Work List", "Focus Hub", "Archived"};

        for (String moduleName : modules) {
            Button navBtn = new Button(moduleName);
            navBtn.getStyleClass().add("nav-button");
            navBtn.setMaxWidth(Double.MAX_VALUE);

            navBtn.setOnAction(e -> {
                setActiveButton(navBtn);
                switchModule(moduleName);
                if (moduleName.equals("Archived")) archivedPanel.refreshList();
            });

            sidebar.getChildren().add(navBtn);
        }

        // --- Push Settings to Bottom ---
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button settingsBtn = new Button("Settings");
        settingsBtn.getStyleClass().add("nav-button");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setOnAction(e -> {
            setActiveButton(settingsBtn);
            switchModule("Settings");
        });
        sidebar.getChildren().add(settingsBtn);

        rootLayout.setLeft(sidebar);
    }

    private void setActiveButton(Button activeBtn) {
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Button) node.getStyleClass().remove("active");
        }
        activeBtn.getStyleClass().add("active");
    }

    private void switchModule(String moduleName) {
        Pane activePane;
        if (moduleName.equals("Quick To-Do")) activePane = quickToDoPanel;
        else if (moduleName.equals("Daily To-Do")) activePane = dailyToDoPanel;
        else if (moduleName.equals("Work List")) activePane = workListPanel;
        else if (moduleName.equals("Focus Hub")) activePane = focusHubPanel;
        else if (moduleName.equals("Archived")) activePane = archivedPanel;
        else if (moduleName.equals("Settings")) activePane = settingsPanel;
        else activePane = new VBox(new Label("Error: Module not found"));

        rootLayout.setCenter(activePane);
    }

    @Override
    public void stop() throws Exception {
        StorageManager.saveTasks(taskDatabase);
        StorageManager.saveStats(appStats);
    }

    public static void main(String[] args) { launch(args); }
}