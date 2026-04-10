package com.raeden.ors_to_do.modules.dependencies.ui.components;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.modules.dependencies.services.AnalyticsExporter;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.DebuffManagerDialog;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.StatHistoryDialog;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FilterSortHeader extends VBox {

    private final SectionConfig config;
    private final AppStats appStats;
    private final List<TaskItem> globalDatabase;

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

        getChildren().addAll(
                buildDashboardStrip(onToggleZen),
                buildFilterSortRow(onFilterSortChanged)
        );
    }

    private HBox buildDashboardStrip(Runnable onToggleZen) {
        HBox dashboardStrip = new HBox(15);
        dashboardStrip.setAlignment(Pos.CENTER_LEFT);
        dashboardStrip.setPadding(new Insets(15));
        dashboardStrip.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox titleBox = new VBox(2);
        titleBox.setMinWidth(Region.USE_PREF_SIZE);

        availableTasksLabel = new Label();
        availableTasksLabel.setMinWidth(Region.USE_PREF_SIZE);
        String titleColor = appStats.isMatchTitleColor() ? config.getSidebarColor() : "#569CD6";
        availableTasksLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");

        activeSubTasksLabel = new Label();
        activeSubTasksLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #858585;");

        HBox subInfoBox = new HBox(10, activeSubTasksLabel);
        subInfoBox.setAlignment(Pos.CENTER_LEFT);

        titleBox.getChildren().addAll(availableTasksLabel, subInfoBox);
        dashboardStrip.getChildren().add(titleBox);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        dashboardStrip.getChildren().add(headerSpacer);

        dashboardStrip.getChildren().add(buildBadgesFlow(onToggleZen));
        return dashboardStrip;
    }

    private FlowPane buildBadgesFlow(Runnable onToggleZen) {
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
            badgesFlow.getChildren().add(buildCountdownTimer());
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

        return badgesFlow;
    }

    private Label buildCountdownTimer() {
        Label countdownLabel = new Label();
        Runnable updateClock = () -> {
            long intervalHours = config.getResetIntervalHours();
            if (intervalHours <= 0) return;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

            long totalMinutes = java.time.Duration.between(startOfDay, now).toMinutes();
            long intervalMins = intervalHours * 60;
            long nextBoundaryMins = ((totalMinutes / intervalMins) + 1) * intervalMins;
            java.time.Duration duration = java.time.Duration.between(now, startOfDay.plusMinutes(nextBoundaryMins));

            // --- FIXED: Scan the database to see if we have tasks to determine text ---
            boolean hasTasks = false;
            if (globalDatabase != null) {
                for (TaskItem t : globalDatabase) {
                    if (config.getId().equals(t.getSectionId()) && !t.isArchived()) {
                        hasTasks = true;
                        break;
                    }
                }
            }

            if (hasTasks) {
                countdownLabel.setText(String.format("Resets in: %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
                countdownLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-background-color: #1E1E1E; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #555555; -fx-border-radius: 15;");
            } else {
                countdownLabel.setText(String.format("Starts in: %02dh %02dm %02ds", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
                countdownLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-background-color: #331A1A; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #8B0000; -fx-border-radius: 15;");
            }
        };

        updateClock.run();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock.run()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        sceneProperty().addListener((obs, old, newScene) -> { if (newScene == null) clock.stop(); });

        return countdownLabel;
    }

    private HBox buildFilterSortRow(Runnable onFilterSortChanged) {
        HBox filterSortRow = new HBox(10);
        filterSortRow.setAlignment(Pos.CENTER_LEFT);
        filterSortRow.setPadding(new Insets(0, 0, 10, 0));

        filterContainer = new FlowPane(5, 5);
        if (config.isShowTags()) filterSortRow.getChildren().add(filterContainer);

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        filterSortRow.getChildren().add(filterSpacer);

        if (config.isStatPage()) {
            Button manageDebuffsBtn = new Button("⚙ Debuff Manager");
            manageDebuffsBtn.setStyle("-fx-background-color: #331A1A; -fx-border-color: #8B0000; -fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: #FF6666; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
            manageDebuffsBtn.setOnAction(e -> DebuffManagerDialog.show(appStats, onFilterSortChanged));
            filterSortRow.getChildren().add(manageDebuffsBtn);

            Button historyBtn = new Button("📖 History");
            historyBtn.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
            historyBtn.setOnAction(e -> StatHistoryDialog.show(appStats, globalDatabase));
            filterSortRow.getChildren().add(historyBtn);
        } else {
            sortComboBox = new ComboBox<>();
            sortComboBox.getItems().addAll("Custom Order", "Most Recent", "Oldest First", "Alphabetical");
            if (config.isShowPriority()) sortComboBox.getItems().addAll("Priority: Low to High", "Priority: High to Low");
            sortComboBox.setValue("Custom Order");

            String css = ".combo-box { -fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; } " +
                    ".combo-box .list-cell { -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: transparent; } " +
                    ".combo-box-popup .list-view { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".combo-box-popup .list-view .list-cell { -fx-background-color: #2D2D30; -fx-text-fill: white; } " +
                    ".combo-box-popup .list-view .list-cell:filled:hover, .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color: #569CD6; } " +
                    ".combo-box .arrow-button { -fx-background-color: transparent; } .combo-box .arrow { -fx-background-color: #AAAAAA; }";

            sortComboBox.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(css.getBytes()));
            sortComboBox.setOnAction(e -> onFilterSortChanged.run());
            filterSortRow.getChildren().add(sortComboBox);
        }
        return filterSortRow;
    }

    public void updateBadges(int availableCount, int completedCount) {
        int[] counts = calculateTrueCounts(availableCount, completedCount);

        if (config.isStatPage()) {
            updateStatPageBadges();
        } else {
            updateStandardPageBadges(counts[0], counts[1]);
        }

        if (scoreLabel != null) scoreLabel.setText("🏆 Score: " + appStats.getGlobalScore());
        updateZenModeBadge(counts[0]);
    }

    private int[] calculateTrueCounts(int fallbackAvail, int fallbackComp) {
        if (globalDatabase == null) return new int[]{fallbackAvail, fallbackComp};

        int avail = 0, comp = 0;
        for (TaskItem task : globalDatabase) {
            if (config.getId().equals(task.getSectionId()) && !task.isArchived() && !task.isOptional()) {
                if (task.isFinished()) comp++; else avail++;
            }
        }
        return new int[]{avail, comp};
    }

    private void updateStatPageBadges() {
        availableTasksLabel.setText("Total Stats: " + appStats.getCustomStats().size());

        int low = 0, high = 0, maxed = 0;
        for (CustomStat stat : appStats.getCustomStats()) {
            double max = stat.getEffectiveMaxCap(appStats.getActiveDebuffs());
            if (max > 0) {
                double pct = (stat.getCurrentAmount() / max) * 100.0;
                if (stat.getCurrentAmount() >= max) maxed++;
                else if (pct > 75) high++;
                else if(pct < 25) low++;
            }
        }

        Label lowLbl = new Label("Low Attributes: " + low);
        lowLbl.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        lowLbl.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #331A1A; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #FF6666; -fx-border-radius: 10;");

        Label highLbl = new Label("High Attributes: " + high);
        highLbl.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        highLbl.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #1A332E; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #4EC9B0; -fx-border-radius: 10;");

        Label maxLbl = new Label("Max Attributes: " + maxed);
        maxLbl.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        maxLbl.setStyle("-fx-text-fill: #569CD6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #1A2633; -fx-padding: 3 8; -fx-background-radius: 10; -fx-border-color: #569CD6; -fx-border-radius: 10;");

        HBox statsBoxRow = new HBox(8, lowLbl, highLbl, maxLbl);
        statsBoxRow.setAlignment(Pos.CENTER_LEFT);
        statsBoxRow.setPadding(new Insets(0, 0, 0, 10));

        if (!(availableTasksLabel.getParent() instanceof HBox)) {
            VBox parent = (VBox) availableTasksLabel.getParent();
            parent.getChildren().remove(availableTasksLabel);
            HBox topRow = new HBox(availableTasksLabel, statsBoxRow);
            topRow.setAlignment(Pos.CENTER_LEFT);
            parent.getChildren().add(0, topRow);
        } else {
            HBox parentHBox = (HBox) availableTasksLabel.getParent();
            if (parentHBox.getChildren().size() > 1) parentHBox.getChildren().set(1, statsBoxRow);
            else parentHBox.getChildren().add(statsBoxRow);
        }

        activeSubTasksLabel.setVisible(false);
        activeSubTasksLabel.setManaged(false);
    }

    private void updateStandardPageBadges(int avail, int comp) {
        if (availableTasksLabel.getParent() instanceof HBox) {
            HBox topRow = (HBox) availableTasksLabel.getParent();
            VBox titleBox = (VBox) topRow.getParent();
            topRow.getChildren().remove(availableTasksLabel);
            titleBox.getChildren().remove(topRow);
            titleBox.getChildren().add(0, availableTasksLabel);
        }

        if (config.isPerkPage()) {
            availableTasksLabel.setText("Total Perks: " + avail);
            activeSubTasksLabel.setText("Active perks: " + comp);
        } else if (config.isChallengePage()) {
            availableTasksLabel.setText("Active Challenge: " + avail);
            activeSubTasksLabel.setText("Completed Challenges: " + comp);
        } else if (config.isNotesPage()) {
            availableTasksLabel.setText("Total Notes: " + avail);
        } else if (config.isHasStreak()) {
            availableTasksLabel.setText(config.getName() + " (" + comp + "/" + (avail + comp) + ")");
        } else {
            availableTasksLabel.setText((config.isRewardsPage() ? "Available Items: " : "Active Tasks: ") + avail);
        }

        boolean showSubLbl = config.isPerkPage() || config.isChallengePage();
        if (!config.isStatPage() && !config.isPerkPage() && !config.isChallengePage() && config.isEnableSubTasks() && !config.isRewardsPage() && !config.isNotesPage()) {
            int activeSubs = 0;
            if (globalDatabase != null) {
                for (TaskItem task : globalDatabase) {
                    if (config.getId().equals(task.getSectionId()) && !task.isFinished() && !task.isArchived()) {
                        for (SubTask sub : task.getSubTasks()) if (!sub.isFinished()) activeSubs++;
                    }
                }
            }
            activeSubTasksLabel.setText("Active sub-tasks: " + activeSubs);
            showSubLbl = true;
        }

        activeSubTasksLabel.setVisible(showSubLbl);
        activeSubTasksLabel.setManaged(showSubLbl);
    }

    private void updateZenModeBadge(int availableCount) {
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

    public void updateFilterPills(Set<String> uniqueTags, Runnable onFilterSortChanged) {
        filterContainer.getChildren().clear();
        ToggleGroup filterGroup = new ToggleGroup();

        String activeStyle = "-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #569CD6; -fx-border-radius: 3; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-border-color: #569CD6; -fx-border-radius: 3; -fx-cursor: hand;";

        ToggleButton allBtn = new ToggleButton("All");
        allBtn.setToggleGroup(filterGroup);
        allBtn.setStyle(activeFilter.equals("All") ? activeStyle : inactiveStyle);
        allBtn.selectedProperty().addListener((o, old, isSel) -> allBtn.setStyle(isSel ? activeStyle : inactiveStyle));
        allBtn.setOnAction(e -> { activeFilter = "All"; onFilterSortChanged.run(); });
        filterContainer.getChildren().add(allBtn);

        List<String> sortedTags = new ArrayList<>(uniqueTags);
        Collections.sort(sortedTags);

        for (String tag : sortedTags) {
            ToggleButton tagBtn = new ToggleButton(tag);
            tagBtn.setToggleGroup(filterGroup);
            tagBtn.setStyle(activeFilter.equals(tag) ? activeStyle : inactiveStyle);
            tagBtn.selectedProperty().addListener((o, old, isSel) -> tagBtn.setStyle(isSel ? activeStyle : inactiveStyle));
            tagBtn.setOnAction(e -> { activeFilter = tagBtn.isSelected() ? tag : "All"; onFilterSortChanged.run(); });
            filterContainer.getChildren().add(tagBtn);
        }
    }

    public String getActiveFilter() { return activeFilter; }
    public String getSortMode() { return sortComboBox != null ? sortComboBox.getValue() : "Custom Order"; }
    public void resetSortMode() { if (sortComboBox != null) sortComboBox.setValue("Custom Order"); }
    public void forceSortMode(String mode) { if (sortComboBox != null) sortComboBox.setValue(mode); }
}