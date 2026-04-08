package com.raeden.ors_to_do.modules.dependencies.ui.components;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.modules.dependencies.services.AnalyticsExporter;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterSortHeader extends VBox {

    private SectionConfig config;
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Label availableTasksLabel;
    private Label activeSubTasksLabel;
    private Label scoreLabel;
    private Button zenModeBtn;
    private FlowPane filterContainer;
    private ComboBox<String> sortComboBox;
    private String activeFilter = "All";

    public FilterSortHeader(SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onToggleZen, Runnable onFilterSortChanged) {
        super(10);
        this.config = config;
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;

        // --- DASHBOARD STRIP ---
        HBox dashboardStrip = new HBox(15);
        dashboardStrip.setAlignment(Pos.CENTER_LEFT);
        dashboardStrip.setPadding(new Insets(15));
        dashboardStrip.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox titleBox = new VBox(2);
        // FORCE the title container to never shrink
        titleBox.setMinWidth(Region.USE_PREF_SIZE);

        availableTasksLabel = new Label();
        // FORCE the title text to never shrink
        availableTasksLabel.setMinWidth(Region.USE_PREF_SIZE);
        String titleColor = appStats.isMatchTitleColor() ? config.getSidebarColor() : "#569CD6";
        availableTasksLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");

        HBox subInfoBox = new HBox(10);
        subInfoBox.setAlignment(Pos.CENTER_LEFT);

        activeSubTasksLabel = new Label();
        activeSubTasksLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #858585;");

        subInfoBox.getChildren().add(activeSubTasksLabel);
        titleBox.getChildren().addAll(availableTasksLabel, subInfoBox);
        dashboardStrip.getChildren().add(titleBox);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        dashboardStrip.getChildren().add(headerSpacer);

        FlowPane badgesFlow = new FlowPane(10, 10);
        badgesFlow.setAlignment(Pos.CENTER_RIGHT);
        badgesFlow.setPrefWrapLength(400);

        if (config.isEnableScore() || config.isRewardsPage()) {
            scoreLabel = new Label();
            scoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-background-color: #332B00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FFD700; -fx-border-radius: 15;");
            badgesFlow.getChildren().add(scoreLabel);
        }

        if (config.isHasStreak()) {
            Label streakLabel = new Label("🔥 Streak: " + config.getCurrentStreak());
            streakLabel.setStyle("-fx-text-fill: #FF8C00; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: #332B00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FF8C00; -fx-border-radius: 15;");
            badgesFlow.getChildren().add(streakLabel);
        }

        if (config.getResetIntervalHours() > 0) {
            Label countdownLabel = new Label();

            Runnable updateClock = () -> {
                long intervalHours = config.getResetIntervalHours();
                if (intervalHours <= 0) return;

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

                long totalMinutesSinceMidnight = java.time.Duration.between(startOfDay, now).toMinutes();
                long intervalMinutes = intervalHours * 60;

                long currentBlockIndex = totalMinutesSinceMidnight / intervalMinutes;
                long minutesIntoCurrentBlock = totalMinutesSinceMidnight % intervalMinutes;

                long nextBoundaryMinutes = (currentBlockIndex + 1) * intervalMinutes;
                LocalDateTime nextBoundary = startOfDay.plusMinutes(nextBoundaryMinutes);
                java.time.Duration duration = java.time.Duration.between(now, nextBoundary);

                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                long seconds = duration.toSecondsPart();

                if (minutesIntoCurrentBlock < 10) {
                    countdownLabel.setText(String.format("Resets in: %02d:%02d:%02d", hours, minutes, seconds));
                    countdownLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-background-color: #1E1E1E; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #555555; -fx-border-radius: 15;");
                } else {
                    countdownLabel.setText(String.format("Starts in: %02dh %02dm %02ds", hours, minutes, seconds));
                    countdownLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-background-color: #331A1A; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #8B0000; -fx-border-radius: 15;");
                }
            };
            updateClock.run();

            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock.run()));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();

            sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) clock.stop();
            });

            badgesFlow.getChildren().add(countdownLabel);
        }

        if (config.isEnableZenMode()) {
            zenModeBtn = new Button("☯ Zen Mode");
            zenModeBtn.setOnAction(e -> onToggleZen.run());
            badgesFlow.getChildren().add(zenModeBtn);
        }

        if (config.isShowAnalytics()) {
            Button exportBtn = new Button("📊 Export");
            exportBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: #569CD6; -fx-border-radius: 15; -fx-font-size: 13px;");
            exportBtn.setOnAction(e -> AnalyticsExporter.exportSectionAnalytics(config, globalDatabase));
            badgesFlow.getChildren().add(exportBtn);
        }

        dashboardStrip.getChildren().add(badgesFlow);
        getChildren().add(dashboardStrip);

        // --- FILTER & SORT ROW ---
        HBox filterSortRow = new HBox(10);
        filterSortRow.setAlignment(Pos.CENTER_LEFT);
        filterSortRow.setPadding(new Insets(0, 0, 10, 0));

        filterContainer = new FlowPane(5, 5);
        if (config.isShowTags()) {
            filterSortRow.getChildren().add(filterContainer);
        }

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        filterSortRow.getChildren().add(filterSpacer);

        if (config.isStatPage()) {
            Button historyBtn = new Button("📖 History");
            historyBtn.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
            historyBtn.setOnAction(e -> showStatHistoryDialog());
            filterSortRow.getChildren().add(historyBtn);
        } else {
            sortComboBox = new ComboBox<>();
            sortComboBox.getItems().addAll("Custom Order", "Most Recent", "Oldest First", "Alphabetical");
            if (config.isShowPriority()) sortComboBox.getItems().addAll("Priority: Low to High", "Priority: High to Low");
            sortComboBox.setValue("Custom Order");

            String css = ".combo-box { -fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; } " +
                    ".combo-box .list-cell { -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: transparent; } " +
                    ".combo-box-popup .list-view { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".combo-box-popup .list-view .list-cell { -fx-background-color: #2D2D30; -fx-text-fill: white; -fx-font-weight: normal; } " +
                    ".combo-box-popup .list-view .list-cell:filled:hover, .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color: #569CD6; -fx-text-fill: white; } " +
                    ".combo-box .arrow-button { -fx-background-color: transparent; } " +
                    ".combo-box .arrow { -fx-background-color: #AAAAAA; }";

            sortComboBox.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(css.getBytes()));
            sortComboBox.setOnAction(e -> onFilterSortChanged.run());

            filterSortRow.getChildren().add(sortComboBox);
        }

        getChildren().add(filterSortRow);
    }

    public void updateBadges(int availableCount, int completedCount) {
        int trueAvailable = 0;
        int trueCompleted = 0;

        if (globalDatabase != null) {
            for (TaskItem task : globalDatabase) {
                if (config.getId().equals(task.getSectionId()) && !task.isArchived()) {
                    if (!task.isOptional()) {
                        if (task.isFinished()) trueCompleted++;
                        else trueAvailable++;
                    }
                }
            }
        } else {
            trueAvailable = availableCount;
            trueCompleted = completedCount;
        }

        if (config.isStatPage()) {
            availableTasksLabel.setText("Total Stats: " + appStats.getCustomStats().size());

            int lowCount = 0; int highCount = 0; int maxedCount = 0;
            for (CustomStat stat : appStats.getCustomStats()) {
                double current = stat.getCurrentAmount();
                double max = stat.getMaxCap();
                if (max > 0) {
                    double percent = (current / max) * 100.0;
                    if (current >= max) maxedCount++;
                    else if (percent > 75) highCount++;
                    else if(percent < 25) lowCount++;
                }
            }

            Label lowLabel = new Label("Low Attributes: " + lowCount);
            lowLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Prevent truncation
            lowLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #331A1A; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #FF6666; -fx-border-radius: 10;");

            Label highLabel = new Label("High Attributes: " + highCount);
            highLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Prevent truncation
            highLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #1A332E; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #4EC9B0; -fx-border-radius: 10;");

            Label maxLabel = new Label("Max Attributes: " + maxedCount);
            maxLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Prevent truncation
            maxLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #1A2633; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #569CD6; -fx-border-radius: 10;");

            HBox statsBoxRow = new HBox(8, lowLabel, highLabel, maxLabel);
            statsBoxRow.setAlignment(Pos.CENTER_LEFT);
            statsBoxRow.setPadding(new Insets(0, 0, 0, 10));

            // Inject right next to the title label
            if (!(availableTasksLabel.getParent() instanceof HBox)) {
                VBox parent = (VBox) availableTasksLabel.getParent();
                parent.getChildren().remove(availableTasksLabel);
                HBox topRow = new HBox(availableTasksLabel, statsBoxRow);
                topRow.setAlignment(Pos.CENTER_LEFT);
                parent.getChildren().add(0, topRow);
            } else {
                HBox parentHBox = (HBox) availableTasksLabel.getParent();
                if (parentHBox.getChildren().size() > 1) {
                    parentHBox.getChildren().set(1, statsBoxRow);
                } else {
                    parentHBox.getChildren().add(statsBoxRow);
                }
            }

            activeSubTasksLabel.setVisible(false);
            activeSubTasksLabel.setManaged(false);

        } else {
            // Restore Layout for Non-Stat Pages
            if (availableTasksLabel.getParent() instanceof HBox) {
                HBox topRow = (HBox) availableTasksLabel.getParent();
                VBox titleBox = (VBox) topRow.getParent();
                topRow.getChildren().remove(availableTasksLabel);
                titleBox.getChildren().remove(topRow);
                titleBox.getChildren().add(0, availableTasksLabel);
            }

            if (config.isPerkPage()) {
                availableTasksLabel.setText("Total Perks: " + availableCount);
                activeSubTasksLabel.setText("Active perks: " + completedCount);
                activeSubTasksLabel.setGraphic(null);
                activeSubTasksLabel.setVisible(true);
                activeSubTasksLabel.setManaged(true);
            } else if (config.isChallengePage()) {
                availableTasksLabel.setText("Active Challenge: " + availableCount);
                activeSubTasksLabel.setText("Completed Challenges: " + completedCount);
                activeSubTasksLabel.setGraphic(null);
                activeSubTasksLabel.setVisible(true);
                activeSubTasksLabel.setManaged(true);
            } else if (config.isNotesPage()) {
                availableTasksLabel.setText("Total Notes: " + availableCount);
                activeSubTasksLabel.setGraphic(null);
                activeSubTasksLabel.setVisible(false);
                activeSubTasksLabel.setManaged(false);
            } else if (config.isHasStreak()) {
                availableTasksLabel.setText(config.getName() + " (" + completedCount + "/" + (availableCount + completedCount) + ")");
                activeSubTasksLabel.setGraphic(null);
                activeSubTasksLabel.setVisible(false);
                activeSubTasksLabel.setManaged(false);
            } else {
                availableTasksLabel.setText((config.isRewardsPage() ? "Available Items: " : "Active Tasks: ") + availableCount);
                activeSubTasksLabel.setGraphic(null);
                activeSubTasksLabel.setVisible(false);
                activeSubTasksLabel.setManaged(false);
            }

            if (!config.isStatPage() && !config.isPerkPage() && !config.isChallengePage() && config.isEnableSubTasks() && !config.isRewardsPage() && !config.isNotesPage()) {
                int activeSubTaskCount = 0;
                if (globalDatabase != null) {
                    for (TaskItem task : globalDatabase) {
                        if (config.getId().equals(task.getSectionId()) && !task.isFinished() && !task.isArchived()) {
                            for (SubTask sub : task.getSubTasks()) {
                                if (!sub.isFinished()) activeSubTaskCount++;
                            }
                        }
                    }
                }
                activeSubTasksLabel.setText("Active sub-tasks: " + activeSubTaskCount);
                activeSubTasksLabel.setVisible(true);
                activeSubTasksLabel.setManaged(true);
            }
        }

        if (scoreLabel != null) scoreLabel.setText("🏆 Score: " + appStats.getGlobalScore());

        if (config.isEnableZenMode() && zenModeBtn != null) {
            if (availableCount >= appStats.getZenModeThreshold()) {
                zenModeBtn.setDisable(false);
                zenModeBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF6666; -fx-background-color: #331A1A; -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: #FF6666; -fx-border-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, #FF4444, 10, 0, 0, 0);");
                zenModeBtn.setText("☯ Zen Mode");
            } else {
                zenModeBtn.setDisable(true);
                zenModeBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555555; -fx-background-color: transparent; -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: #3E3E42; -fx-border-radius: 15;");
                zenModeBtn.setText("☯ Zen Mode (" + availableCount + "/" + appStats.getZenModeThreshold() + ")");
            }
        }
    }

    private void showStatHistoryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recent Stat Gains History");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        boolean hasHistory = false;
        for (int i = globalDatabase.size() - 1; i >= 0; i--) {
            TaskItem task = globalDatabase.get(i);

            if (task.isFinished() && task.getStatRewards() != null && !task.getStatRewards().isEmpty()) {
                hasHistory = true;

                String dateStr = "Unknown Date";
                if (task.getPerkUnlockedDate() != null) dateStr = task.getPerkUnlockedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                else if (task.getDateCreated() != null) dateStr = task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

                VBox entryBox = new VBox(5);
                entryBox.setStyle("-fx-background-color: #2D2D30; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #3E3E42;");

                Label sourceLabel = new Label("From: " + task.getTextContent());
                sourceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label dateLabel = new Label("Completed: " + dateStr);
                dateLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px;");

                HBox rewardsBox = new HBox(10);
                for (Map.Entry<String, Integer> reward : task.getStatRewards().entrySet()) {
                    CustomStat stat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(reward.getKey())).findFirst().orElse(null);
                    if (stat != null) {
                        Label rLbl = new Label("+" + reward.getValue() + " " + stat.getName());
                        rLbl.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold; -fx-background-color: #1A332E; -fx-padding: 2 6; -fx-background-radius: 5;");
                        rewardsBox.getChildren().add(rLbl);
                    }
                }

                entryBox.getChildren().addAll(sourceLabel, dateLabel, rewardsBox);
                content.getChildren().add(entryBox);
            }
        }

        if (!hasHistory) {
            Label empty = new Label("No recent tasks found that granted custom stats.");
            empty.setStyle("-fx-text-fill: #858585; -fx-font-style: italic;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(450, 500);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scroll.setBorder(Border.EMPTY);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void updateFilterPills(Set<String> uniqueTags, Runnable onFilterSortChanged) {
        filterContainer.getChildren().clear();
        ToggleGroup filterGroup = new ToggleGroup();

        String activeAllStyle = "-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
        String inactiveAllStyle = "-fx-background-color: #3E3E42; -fx-text-fill: #AAAAAA; -fx-cursor: hand;";

        String activeTagStyle = "-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #569CD6; -fx-border-radius: 3; -fx-cursor: hand;";
        String inactiveTagStyle = "-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-border-color: #569CD6; -fx-border-radius: 3; -fx-cursor: hand;";

        ToggleButton allBtn = new ToggleButton("All");
        allBtn.setToggleGroup(filterGroup);

        if (activeFilter.equals("All")) {
            allBtn.setSelected(true);
            allBtn.setStyle(activeAllStyle);
        } else {
            allBtn.setStyle(inactiveAllStyle);
        }

        allBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            allBtn.setStyle(isSelected ? activeAllStyle : inactiveAllStyle);
        });

        allBtn.setOnAction(e -> {
            activeFilter = "All";
            onFilterSortChanged.run();
        });
        filterContainer.getChildren().add(allBtn);

        List<String> sortedTags = new ArrayList<>(uniqueTags);
        Collections.sort(sortedTags);

        for (String tag : sortedTags) {
            ToggleButton tagBtn = new ToggleButton(tag);
            tagBtn.setToggleGroup(filterGroup);

            if (activeFilter.equals(tag)) {
                tagBtn.setSelected(true);
                tagBtn.setStyle(activeTagStyle);
            } else {
                tagBtn.setStyle(inactiveTagStyle);
            }

            tagBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                tagBtn.setStyle(isSelected ? activeTagStyle : inactiveTagStyle);
            });

            tagBtn.setOnAction(e -> {
                if (tagBtn.isSelected()) {
                    activeFilter = tag;
                } else {
                    activeFilter = "All";
                }
                onFilterSortChanged.run();
            });
            filterContainer.getChildren().add(tagBtn);
        }
    }

    public String getActiveFilter() { return activeFilter; }
    public String getSortMode() { return sortComboBox != null ? sortComboBox.getValue() : "Custom Order"; }
    public void resetSortMode() { if (sortComboBox != null) sortComboBox.setValue("Custom Order"); }
    public void forceSortMode(String mode) { if (sortComboBox != null) sortComboBox.setValue(mode); }
}