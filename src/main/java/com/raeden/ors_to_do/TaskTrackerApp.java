package com.raeden.ors_to_do;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.*;
import com.raeden.ors_to_do.modules.dependencies.DailyRolloverManager;
import com.raeden.ors_to_do.modules.dependencies.SystemTrayManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class TaskTrackerApp extends Application implements NativeKeyListener {

    private List<TaskItem> taskDatabase;
    private AppStats appStats;
    private BorderPane rootLayout;
    private VBox sidebar;

    private FocusHubModule focusHubPanel;
    private AnalyticsModule analyticsPanel;
    private ArchivedModule archivedPanel;
    private SettingsModule settingsPanel;
    private DynamicModule currentDynamicPanel;

    private String currentActiveModule = "QUICK";
    private boolean isFirstMinimize = true;
    private static final int INSTANCE_PORT = 44444;

    @Override
    public void init() throws Exception {
        taskDatabase = StorageManager.loadTasks();
        appStats = StorageManager.loadStats();

        runSilentDataMigration();
        DailyRolloverManager.processDailyRollover(appStats, taskDatabase);

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }

    private void runSilentDataMigration() {
        boolean needsSave = false;

        if (appStats.getSections().isEmpty()) {
            AppStats.SectionConfig quick = new AppStats.SectionConfig("QUICK", appStats.getNavQuickText());
            quick.setShowPriority(true); quick.setEnableSubTasks(true); quick.setAllowArchive(true); quick.setShowDate(true);

            AppStats.SectionConfig daily = new AppStats.SectionConfig("DAILY", appStats.getNavDailyText());
            daily.setResetIntervalHours(24);
            daily.setHasStreak(true); daily.setShowPrefix(true); daily.setAllowArchive(true);

            AppStats.SectionConfig work = new AppStats.SectionConfig("WORK", appStats.getNavWorkText());
            work.setShowAnalytics(true); work.setEnableSubTasks(true); work.setShowPriority(true); work.setShowWorkType(true);
            work.setTrackTime(true); work.setAllowArchive(true); work.setShowTags(true); work.setShowDate(true);

            appStats.getSections().addAll(List.of(quick, daily, work));
            needsSave = true;
        }

        if (!appStats.getBaseDailies().isEmpty()) {
            Optional<AppStats.SectionConfig> dailyConfig = appStats.getSections().stream().filter(s -> "DAILY".equals(s.getId())).findFirst();
            if (dailyConfig.isPresent() && dailyConfig.get().getAutoAddTemplates().isEmpty()) {
                dailyConfig.get().getAutoAddTemplates().addAll(appStats.getBaseDailies());
                appStats.getBaseDailies().clear();
                needsSave = true;
            }
        }

        for (TaskItem task : taskDatabase) {
            if (task.getSectionId() == null && task.getLegacyOriginModule() != null) {
                task.setSectionId(task.getLegacyOriginModule().name());
                needsSave = true;
            }
        }

        if (needsSave) {
            StorageManager.saveStats(appStats);
            StorageManager.saveTasks(taskDatabase);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        startSingleInstanceServer(primaryStage);
        Platform.setImplicitExit(false);
        SystemTrayManager.setupSystemTray(primaryStage, this::shutdownApp);

        primaryStage.setOnCloseRequest(event -> {
            DailyRolloverManager.autoArchiveTasks(appStats, taskDatabase);
            StorageManager.saveTasks(taskDatabase);
            StorageManager.saveStats(appStats);

            if (appStats.isRunInBackground() && java.awt.SystemTray.isSupported()) {
                event.consume();
                primaryStage.hide();

                if (isFirstMinimize) {
                    SystemTrayManager.pushNotification("Running in Background", "Task Tracker is still running. Double-click the tray icon to restore.");
                    isFirstMinimize = false;
                }
            } else {
                shutdownApp();
            }
        });

        rootLayout = new BorderPane();
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Runnable syncUI = () -> {
            if (currentDynamicPanel != null) currentDynamicPanel.refreshList();
            if (focusHubPanel != null) focusHubPanel.refreshTasks();
            if (analyticsPanel != null) analyticsPanel.refreshData();
            setupSidebar();
        };

        focusHubPanel = new FocusHubModule(appStats, taskDatabase, syncUI);
        analyticsPanel = new AnalyticsModule(appStats, taskDatabase);
        archivedPanel = new ArchivedModule(taskDatabase, appStats, syncUI);
        settingsPanel = new SettingsModule(appStats, taskDatabase, syncUI);

        setupSidebar();
        rootLayout.setLeft(sidebar);

        Scene scene = new Scene(rootLayout, 1000, 700);

        // --- FIX: Safe CSS Loading ---
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: styles.css not found. App will run with default JavaFX styling.");
        }

        primaryStage.setTitle("Task-Tracker");
        primaryStage.setAlwaysOnTop(appStats.isAlwaysOnTop());

        // --- FIX: Safe Icon Loading ---
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                System.out.println("Warning: Window icon.png not found.");
            }
        } catch (Exception e) {
            System.out.println("Error loading window icon.");
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        if (!appStats.getSections().isEmpty()) switchModule(appStats.getSections().get(0).getId());
        else switchModule("SETTINGS");
    }

    private void shutdownApp() {
        try {
            stop();
            Platform.exit();
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startSingleInstanceServer(Stage primaryStage) {
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(INSTANCE_PORT, 1, InetAddress.getByName("127.0.0.1"));
                while (true) {
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg = in.readLine();
                    if ("WAKE_UP".equals(msg)) {
                        Platform.runLater(() -> {
                            // --- FIX: Actually wake up the window! ---
                            if (primaryStage != null) {
                                primaryStage.show();
                                primaryStage.setIconified(false);
                                primaryStage.toFront();
                            }
                        });
                    }
                    client.close();
                }
            } catch (Exception e) {}
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE
                && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0
                && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) {
            Platform.runLater(this::showQuickCaptureOverlay);
        }
    }

    private void showQuickCaptureOverlay() {
        Stage captureStage = new Stage();
        captureStage.initStyle(StageStyle.TRANSPARENT);
        captureStage.setAlwaysOnTop(true);

        TextField captureField = new TextField();
        captureField.setPromptText("Quick Capture...");
        captureField.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 15px; -fx-border-color: #569CD6; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        captureField.setPrefWidth(400);

        captureField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !captureField.getText().trim().isEmpty()) {
                TaskItem.CustomPriority defaultPrio = appStats.getCustomPriorities().isEmpty() ? null : appStats.getCustomPriorities().get(0);
                String fallbackId = appStats.getSections().isEmpty() ? "QUICK" : appStats.getSections().get(0).getId();

                TaskItem newTask = new TaskItem(captureField.getText().trim(), defaultPrio, fallbackId);
                taskDatabase.add(newTask);
                StorageManager.saveTasks(taskDatabase);

                if (currentDynamicPanel != null) currentDynamicPanel.refreshList();
                captureStage.close();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                captureStage.close();
            }
        });

        captureField.focusedProperty().addListener((obs, oldV, newV) -> { if (!newV) captureStage.close(); });
        VBox layout = new VBox(captureField); layout.setStyle("-fx-background-color: transparent; -fx-padding: 10;");
        Scene scene = new Scene(layout); scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        captureStage.setScene(scene); captureStage.show(); captureField.requestFocus();
    }

    private void setupSidebar() {
        sidebar.getChildren().clear();

        for (AppStats.SectionConfig config : appStats.getSections()) {
            addSidebarButton(config.getName(), config.getId(), config.getSidebarColor());
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
        sidebar.getChildren().add(sep);

        addSidebarButton(appStats.getNavFocusText(), "FOCUS", appStats.getNavFocusColor());
        addSidebarButton(appStats.getNavAnalyticsText(), "ANALYTICS", appStats.getNavAnalyticsColor());
        addSidebarButton(appStats.getNavArchiveText(), "ARCHIVE", appStats.getNavArchiveColor());
        addSidebarButton(appStats.getNavSettingsText(), "SETTINGS", appStats.getNavSettingsColor());
    }

    private void addSidebarButton(String displayText, String internalId, String hexColor) {
        Button btn = new Button(displayText);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(5, 20);
        rect.setArcWidth(3); rect.setArcHeight(3);
        rect.setFill(javafx.scene.paint.Color.web(hexColor != null ? hexColor : "#FFFFFF"));

        btn.setGraphic(rect);
        btn.setGraphicTextGap(10);

        if (currentActiveModule.equals(internalId)) btn.getStyleClass().add("active");

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
        Node activePane = null;

        if (internalId.equals("FOCUS")) {
            activePane = focusHubPanel;
            focusHubPanel.refreshTasks();
            currentDynamicPanel = null;
        } else if (internalId.equals("ANALYTICS")) {
            activePane = analyticsPanel;
            analyticsPanel.refreshData();
            currentDynamicPanel = null;
        } else if (internalId.equals("ARCHIVE")) {
            activePane = archivedPanel;
            currentDynamicPanel = null;
        } else if (internalId.equals("SETTINGS")) {
            activePane = settingsPanel;
            currentDynamicPanel = null;
        } else {
            Optional<AppStats.SectionConfig> matchedConfig = appStats.getSections().stream()
                    .filter(c -> c.getId().equals(internalId))
                    .findFirst();

            if (matchedConfig.isPresent()) {
                Runnable syncUI = () -> {
                    if (currentDynamicPanel != null) currentDynamicPanel.refreshList();
                    if (focusHubPanel != null) focusHubPanel.refreshTasks();
                    setupSidebar();
                };

                currentDynamicPanel = new DynamicModule(matchedConfig.get(), taskDatabase, appStats, syncUI);
                activePane = currentDynamicPanel;
            } else {
                activePane = new VBox(new Label("Error: Section Configuration Not Found for " + internalId));
                currentDynamicPanel = null;
            }
        }

        if (activePane != null) rootLayout.setCenter(activePane);
    }

    @Override
    public void stop() throws Exception {
        DailyRolloverManager.autoArchiveTasks(appStats, taskDatabase); // DELEGATED
        StorageManager.saveTasks(taskDatabase);
        StorageManager.saveStats(appStats);

        try { GlobalScreen.unregisterNativeHook(); }
        catch (NativeHookException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), INSTANCE_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("WAKE_UP");
            socket.close();

            System.out.println("App is already running in background. Waking up existing instance and closing this one.");
            System.exit(0);
            return;
        } catch (Exception e) {}

        launch(args);
    }
}