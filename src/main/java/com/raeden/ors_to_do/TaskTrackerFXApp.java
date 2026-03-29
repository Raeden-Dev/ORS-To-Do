package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.*;
import javafx.application.Application;
import javafx.scene.Node;
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

    private String currentActiveModule = "QUICK"; // Tracks internal ID

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
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Runnable syncUI = () -> {
            if (quickToDoPanel != null) quickToDoPanel.refreshList();
            if (workListPanel != null) workListPanel.refreshList();
            if (dailyToDoPanel != null) dailyToDoPanel.refreshList();
            setupSidebar(); // Rebuilds sidebar to reflect name changes instantly
        };

        quickToDoPanel = new TaskModuleFX(TaskItem.OriginModule.QUICK, taskDatabase, appStats);
        dailyToDoPanel = new DailyModuleFX(taskDatabase, appStats);
        workListPanel = new TaskModuleFX(TaskItem.OriginModule.WORK, taskDatabase, appStats);
        focusHubPanel = new FocusHubModuleFX(appStats);
        archivedPanel = new ArchivedModuleFX(taskDatabase, syncUI);
        settingsPanel = new SettingsModuleFX(appStats, taskDatabase, syncUI);

        setupSidebar();
        rootLayout.setLeft(sidebar);

        Scene scene = new Scene(rootLayout, 1000, 700);
        String css = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Task-Tracker (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();

        switchModule("QUICK"); // Load default
    }

    private void setupSidebar() {
        sidebar.getChildren().clear();

        addSidebarButton(appStats.getNavQuickText(), "QUICK");
        addSidebarButton(appStats.getNavDailyText(), "DAILY");
        addSidebarButton(appStats.getNavWorkText(), "WORK");
        addSidebarButton(appStats.getNavFocusText(), "FOCUS");
        addSidebarButton(appStats.getNavArchiveText(), "ARCHIVE");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        addSidebarButton(appStats.getNavSettingsText(), "SETTINGS");
    }

    private void addSidebarButton(String displayText, String internalId) {
        Button btn = new Button(displayText);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);

        if (currentActiveModule.equals(internalId)) {
            btn.getStyleClass().add("active");
        }

        btn.setOnAction(e -> {
            currentActiveModule = internalId;
            setActiveButton(btn);
            switchModule(internalId);
            if (internalId.equals("ARCHIVE")) archivedPanel.refreshList();
        });
        sidebar.getChildren().add(btn);
    }

    private void setActiveButton(Button activeBtn) {
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Button) node.getStyleClass().remove("active");
        }
        activeBtn.getStyleClass().add("active");
    }

    private void switchModule(String internalId) {
        Node activePane;
        if (internalId.equals("QUICK")) activePane = quickToDoPanel;
        else if (internalId.equals("DAILY")) activePane = dailyToDoPanel;
        else if (internalId.equals("WORK")) activePane = workListPanel;
        else if (internalId.equals("FOCUS")) activePane = focusHubPanel;
        else if (internalId.equals("ARCHIVE")) activePane = archivedPanel;
        else if (internalId.equals("SETTINGS")) activePane = settingsPanel;
        else activePane = new VBox(new Label("Error: Module not found"));

        rootLayout.setCenter(activePane);
    }

    private void autoArchiveTasks() {
        for (TaskItem task : taskDatabase) {
            // Target BOTH Quick and Work tasks that are finished and not yet archived
            if ((task.getOriginModule() == TaskItem.OriginModule.QUICK || task.getOriginModule() == TaskItem.OriginModule.WORK)
                && task.isFinished() && !task.isArchived()) {

                task.setArchived(true);

                // Failsafe: Ensure Date Completed is populated
                if (task.getDateCompleted() == null) {
                    task.setFinished(true);
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        autoArchiveTasks();
        StorageManager.saveTasks(taskDatabase);
        StorageManager.saveStats(appStats);
    }

    public static void main(String[] args) { launch(args); }
}