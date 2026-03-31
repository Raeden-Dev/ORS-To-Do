package com.raeden.ors_to_do;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.*;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TaskTrackerFXApp extends Application implements NativeKeyListener {

    private List<TaskItem> taskDatabase;
    private AppStats appStats;
    private BorderPane rootLayout;
    private VBox sidebar;

    // --- Static Core Panels ---
    private FocusHubModuleFX focusHubPanel;
    private ArchivedModuleFX archivedPanel;
    private SettingsModuleFX settingsPanel;

    // --- NEW: The currently loaded dynamic list ---
    private DynamicModuleFX currentDynamicPanel;

    private String currentActiveModule = "QUICK";

    private static java.awt.TrayIcon trayIcon;
    private boolean isFirstMinimize = true;

    private static Stage mainStage;
    private static final int INSTANCE_PORT = 44444;

    @Override
    public void init() throws Exception {
        taskDatabase = StorageManager.loadTasks();
        appStats = StorageManager.loadStats();

        runSilentDataMigration(); // From Phase 1
        processDailyRollover();

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
            quick.setShowPriority(true);
            quick.setEnableSubTasks(true);
            quick.setAllowArchive(true);
            quick.setShowDate(true);

            AppStats.SectionConfig daily = new AppStats.SectionConfig("DAILY", appStats.getNavDailyText());
            daily.setHasStreak(true);
            daily.setShowPrefix(true);
            daily.setAllowArchive(true);

            AppStats.SectionConfig work = new AppStats.SectionConfig("WORK", appStats.getNavWorkText());
            work.setShowAnalytics(true);
            work.setEnableSubTasks(true);
            work.setShowPriority(true);
            work.setShowWorkType(true);
            work.setTrackTime(true);
            work.setAllowArchive(true);
            work.setShowTags(true);
            work.setShowDate(true);

            appStats.getSections().addAll(List.of(quick, daily, work));
            needsSave = true;
            System.out.println("Migrated AppStats: Generated default Section Configs.");
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
        mainStage = primaryStage;
        startSingleInstanceServer();

        Platform.setImplicitExit(false);
        setupSystemTray(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            autoArchiveTasks();
            StorageManager.saveTasks(taskDatabase);
            StorageManager.saveStats(appStats);

            if (appStats.isRunInBackground() && java.awt.SystemTray.isSupported()) {
                event.consume();
                primaryStage.hide();

                if (isFirstMinimize) {
                    pushNotification("Running in Background", "Task Tracker is still running. Double-click the tray icon to restore.");
                    isFirstMinimize = false;
                }
            } else {
                try {
                    stop();
                    Platform.exit();
                    System.exit(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        rootLayout = new BorderPane();
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Runnable syncUI = () -> {
            if (currentDynamicPanel != null) currentDynamicPanel.refreshList();
            if (focusHubPanel != null) focusHubPanel.refreshTasks();
            setupSidebar();
        };

        focusHubPanel = new FocusHubModuleFX(appStats, taskDatabase, syncUI);
        archivedPanel = new ArchivedModuleFX(taskDatabase, appStats, syncUI);
        settingsPanel = new SettingsModuleFX(appStats, taskDatabase, syncUI);

        setupSidebar();
        rootLayout.setLeft(sidebar);

        Scene scene = new Scene(rootLayout, 1000, 700);
        String css = getClass().getResource("/styles.css").toExternalForm();
        if (css != null) scene.getStylesheets().add(css);

        primaryStage.setTitle("Task-Tracker (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Boot into the first available section
        if (!appStats.getSections().isEmpty()) {
            switchModule(appStats.getSections().get(0).getId());
        } else {
            switchModule("SETTINGS");
        }
    }

    private void startSingleInstanceServer() {
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(INSTANCE_PORT, 1, InetAddress.getByName("127.0.0.1"));
                while (true) {
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg = in.readLine();
                    if ("WAKE_UP".equals(msg)) {
                        Platform.runLater(() -> {
                            if (mainStage != null) {
                                mainStage.show();
                                mainStage.setIconified(false);
                                mainStage.toFront();
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

    private void setupSystemTray(Stage primaryStage) {
        if (!java.awt.SystemTray.isSupported()) return;

        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = image.createGraphics();
        g2d.setColor(new java.awt.Color(86, 156, 214));
        g2d.fillOval(0, 0, 16, 16);
        g2d.dispose();

        trayIcon = new java.awt.TrayIcon(image, "ORS Task Tracker");
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Platform.runLater(() -> {
                        primaryStage.show();
                        primaryStage.setIconified(false);
                        primaryStage.toFront();
                    });
                }
            }
        });

        java.awt.PopupMenu popup = new java.awt.PopupMenu();
        java.awt.MenuItem openItem = new java.awt.MenuItem("Open Task Tracker");
        openItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.setIconified(false);
            primaryStage.toFront();
        }));

        java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit Entirely");
        exitItem.addActionListener(e -> {
            Platform.runLater(() -> {
                try {
                    stop();
                    Platform.exit();
                    System.exit(0);
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        try { tray.add(trayIcon); }
        catch (java.awt.AWTException e) { System.err.println("TrayIcon could not be added."); }
    }

    public static void pushNotification(String title, String message) {
        if (mainStage != null && !mainStage.isShowing()) {
            if (trayIcon != null) trayIcon.displayMessage(title, message, java.awt.TrayIcon.MessageType.INFO);
        } else {
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.show();
            });
        }
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

                // Automatically route to the very first section configured in your app
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

        captureField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) captureStage.close();
        });

        VBox layout = new VBox(captureField);
        layout.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        Scene scene = new Scene(layout);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        captureStage.setScene(scene);
        captureStage.show();
        captureField.requestFocus();
    }

    private void processDailyRollover() {
        LocalDate today = LocalDate.now();
        LocalDate lastOpened = appStats.getLastOpenedDate();

        if (today.isAfter(lastOpened)) {
            int totalDaily = 0;
            int completedDaily = 0;

            for (TaskItem task : taskDatabase) {
                if ("DAILY".equals(task.getSectionId()) && !task.isArchived()) {
                    totalDaily++;
                    if (task.isFinished()) completedDaily++;
                }
            }

            if (totalDaily > 0) {
                double percentComplete = (double) completedDaily / totalDaily;
                appStats.addHistoryRecord(lastOpened, percentComplete);
                appStats.getAdvancedHistoryLog().put(lastOpened, new int[]{totalDaily, completedDaily});

                double requiredFraction = appStats.getMinDailyCompletionPercent() / 100.0;

                if (percentComplete >= (requiredFraction - 0.001)) {
                    appStats.setCurrentStreak(appStats.getCurrentStreak() + 1);
                } else {
                    appStats.setCurrentStreak(0);
                }
            }

            for (TaskItem task : taskDatabase) {
                if ("DAILY".equals(task.getSectionId()) && !task.isArchived()) {
                    task.setArchived(true);
                    if (task.getDateCompleted() == null) task.setFinished(true);
                }
            }

            TaskItem.CustomPriority defaultPrio = appStats.getCustomPriorities().isEmpty() ? null : appStats.getCustomPriorities().get(0);
            for (AppStats.DailyTemplate template : appStats.getBaseDailies()) {
                TaskItem newTask = new TaskItem(template.getText(), defaultPrio, "DAILY");
                if (template.getPrefix() != null && !template.getPrefix().isEmpty()) newTask.setPrefix(template.getPrefix());

                newTask.setPrefixColor(template.getPrefixColor());
                if (template.getBgColor() != null) newTask.setColorHex(template.getBgColor());

                taskDatabase.add(newTask);
            }

            appStats.setLastOpenedDate(today);
            StorageManager.saveStats(appStats);
            StorageManager.saveTasks(taskDatabase);
        }
    }

    private void setupSidebar() {
        sidebar.getChildren().clear();

        // 1. Dynamic Section Buttons
        for (AppStats.SectionConfig config : appStats.getSections()) {
            addSidebarButton(config.getName(), config.getId(), config.getSidebarColor());
        }

        // 2. Flexible Spacer pushes the rest to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // 3. Separator Line
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
        sidebar.getChildren().add(sep);

        // 4. Static Bottom Buttons
        addSidebarButton(appStats.getNavFocusText(), "FOCUS", appStats.getNavFocusColor());
        addSidebarButton(appStats.getNavArchiveText(), "ARCHIVE", appStats.getNavArchiveColor());
        addSidebarButton(appStats.getNavSettingsText(), "SETTINGS", appStats.getNavSettingsColor());
    }

    private void addSidebarButton(String displayText, String internalId, String hexColor) {
        Button btn = new Button(displayText);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT); // Ensure text sits next to the rectangle

        // --- NEW: Custom Side Rectangle Graphic ---
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(5, 20);
        rect.setArcWidth(3); rect.setArcHeight(3);
        rect.setFill(javafx.scene.paint.Color.web(hexColor != null ? hexColor : "#FFFFFF"));

        btn.setGraphic(rect);
        btn.setGraphicTextGap(10); // Space between rectangle and text

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

    // --- NEW: Dynamic Routing Engine ---
    private void switchModule(String internalId) {
        Node activePane = null;

        if (internalId.equals("FOCUS")) {
            activePane = focusHubPanel;
            focusHubPanel.refreshTasks();
            currentDynamicPanel = null;
        } else if (internalId.equals("ARCHIVE")) {
            activePane = archivedPanel;
            currentDynamicPanel = null;
        } else if (internalId.equals("SETTINGS")) {
            activePane = settingsPanel;
            currentDynamicPanel = null;
        } else {
            // It's a dynamic user-created section! Find it and build it.
            Optional<AppStats.SectionConfig> matchedConfig = appStats.getSections().stream()
                    .filter(c -> c.getId().equals(internalId))
                    .findFirst();

            if (matchedConfig.isPresent()) {
                Runnable syncUI = () -> {
                    if (currentDynamicPanel != null) currentDynamicPanel.refreshList();
                    if (focusHubPanel != null) focusHubPanel.refreshTasks();
                    setupSidebar();
                };

                // Instantiate the chameleon UI layout with this specific configuration
                currentDynamicPanel = new DynamicModuleFX(matchedConfig.get(), taskDatabase, appStats, syncUI);
                activePane = currentDynamicPanel;
            } else {
                activePane = new VBox(new Label("Error: Section Configuration Not Found for " + internalId));
                currentDynamicPanel = null;
            }
        }

        if (activePane != null) {
            rootLayout.setCenter(activePane);
        }
    }

    private void autoArchiveTasks() {
        for (TaskItem task : taskDatabase) {
            // Find the config for this specific task's section
            Optional<AppStats.SectionConfig> matchedConfig = appStats.getSections().stream()
                    .filter(c -> c.getId().equals(task.getSectionId()))
                    .findFirst();

            // If the section allows auto-archive, archive it!
            if (matchedConfig.isPresent() && matchedConfig.get().isAllowArchive()) {
                if (task.isFinished() && !task.isArchived()) {
                    task.setArchived(true);
                    if (task.getDateCompleted() == null) task.setFinished(true);
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        autoArchiveTasks();
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