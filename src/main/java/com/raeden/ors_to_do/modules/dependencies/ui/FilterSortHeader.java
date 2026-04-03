package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.services.AnalyticsExporter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FilterSortHeader extends VBox {

    private SectionConfig config;
    private AppStats appStats;
    private Label availableTasksLabel;
    private Label scoreLabel;
    private Button zenModeBtn;
    private FlowPane filterContainer;
    private ComboBox<String> sortComboBox;
    private String activeFilter = "All";

    public FilterSortHeader(SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onToggleZen, Runnable onFilterSortChanged) {
        super(10);
        this.config = config;
        this.appStats = appStats;

        // --- DASHBOARD STRIP ---
        HBox dashboardStrip = new HBox(15);
        dashboardStrip.setAlignment(Pos.CENTER_LEFT);
        dashboardStrip.setPadding(new Insets(15));
        dashboardStrip.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        availableTasksLabel = new Label();
        String titleColor = appStats.isMatchTitleColor() ? config.getSidebarColor() : "#569CD6";
        availableTasksLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");
        dashboardStrip.getChildren().add(availableTasksLabel);

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
            Label streakLabel = new Label("🔥 " + appStats.getCurrentStreak() + " Day Streak");
            streakLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF8C00; -fx-background-color: #331A00; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #FF8C00; -fx-border-radius: 15;");

            Label countdownLabel = new Label();
            countdownLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-background-color: #1E1E1E; -fx-padding: 5 10; -fx-background-radius: 15; -fx-border-color: #555555; -fx-border-radius: 15;");

            Runnable updateClock = () -> {
                java.time.Duration duration = java.time.Duration.between(LocalTime.now(), LocalTime.MAX);
                countdownLabel.setText(String.format("Resets in: %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
            };
            updateClock.run();

            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock.run()));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();

            sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) clock.stop();
            });

            badgesFlow.getChildren().addAll(streakLabel, countdownLabel);
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

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Custom Order", "Most Recent", "Oldest First", "Alphabetical");
        if (config.isShowPriority()) sortComboBox.getItems().addAll("Priority: Low to High", "Priority: High to Low");
        sortComboBox.setValue("Custom Order");
        sortComboBox.setStyle("-fx-background-color: #E0E0E0; -fx-cursor: hand;");

        sortComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
            }
        });
        sortComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: black;"); }
            }
        });

        sortComboBox.setOnAction(e -> onFilterSortChanged.run());

        filterSortRow.getChildren().addAll(filterSpacer, sortComboBox);
        getChildren().add(filterSortRow);
    }

    public void updateBadges(int availableCount, int completedCount) {
        if (config.isHasStreak()) availableTasksLabel.setText(config.getName() + " (" + completedCount + "/" + (availableCount + completedCount) + ")");
        else availableTasksLabel.setText((config.isRewardsPage() ? "Available Items: " : "Active Tasks: ") + availableCount);

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

    public void updateFilterPills(Set<String> uniqueTags, Runnable onFilterSortChanged) {
        filterContainer.getChildren().clear();
        ToggleGroup filterGroup = new ToggleGroup();

        ToggleButton allBtn = new ToggleButton("All");
        allBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
        allBtn.setToggleGroup(filterGroup);
        if (activeFilter.equals("All")) allBtn.setSelected(true);
        allBtn.setOnAction(e -> { activeFilter = "All"; onFilterSortChanged.run(); });
        filterContainer.getChildren().add(allBtn);

        List<String> sortedTags = new ArrayList<>(uniqueTags);
        Collections.sort(sortedTags);

        for (String tag : sortedTags) {
            ToggleButton tagBtn = new ToggleButton(tag);
            tagBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-border-color: #569CD6; -fx-border-radius: 3; -fx-cursor: hand;");
            tagBtn.setToggleGroup(filterGroup);
            if (activeFilter.equals(tag)) tagBtn.setSelected(true);

            tagBtn.setOnAction(e -> {
                if (tagBtn.isSelected()) activeFilter = tag;
                else activeFilter = "All";
                onFilterSortChanged.run();
            });
            filterContainer.getChildren().add(tagBtn);
        }
    }

    public String getActiveFilter() { return activeFilter; }
    public String getSortMode() { return sortComboBox.getValue(); }
    public void resetSortMode() { sortComboBox.setValue("Custom Order"); }
    public void forceSortMode(String mode) { sortComboBox.setValue(mode); }
}