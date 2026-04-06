package com.raeden.ors_to_do.modules.dependencies.ui.analytics;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Map;

public class AnalyticsSectionBreakdown extends VBox {

    public AnalyticsSectionBreakdown(AppStats appStats, Map<String, Integer> sectionTasksMap, Map<String, Integer> sectionTimeMap) {
        super(10);

        Label breakdownLabel = new Label("Section Breakdown");
        breakdownLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E0E0E0;");
        breakdownLabel.setPadding(new Insets(20, 0, 5, 0));
        getChildren().add(breakdownLabel);

        VBox breakdownList = new VBox(10);

        for (SectionConfig config : appStats.getSections()) {
            int tasks = sectionTasksMap.getOrDefault(config.getId(), 0);
            int timeSecs = sectionTimeMap.getOrDefault(config.getId(), 0);
            if (tasks > 0 || timeSecs > 0) {
                breakdownList.getChildren().add(createSectionRow(config.getName(), config.getSidebarColor(), tasks, timeSecs));
            }
        }

        int legacyTasks = sectionTasksMap.getOrDefault("Unknown", 0);
        int legacyTime = sectionTimeMap.getOrDefault("Unknown", 0);
        if (legacyTasks > 0 || legacyTime > 0) {
            breakdownList.getChildren().add(createSectionRow("Legacy/Deleted Sections", "#858585", legacyTasks, legacyTime));
        }

        if (breakdownList.getChildren().isEmpty()) {
            Label empty = new Label("No data available yet. Start crushing tasks!");
            empty.setStyle("-fx-text-fill: #858585; -fx-font-style: italic;");
            breakdownList.getChildren().add(empty);
        }

        getChildren().add(breakdownList);
    }

    private HBox createSectionRow(String sectionName, String colorHex, int tasksCompleted, int timeSecs) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-background-radius: 5;");

        Rectangle colorRect = new Rectangle(12, 12);
        colorRect.setArcWidth(3); colorRect.setArcHeight(3);
        colorRect.setFill(Color.web(colorHex != null ? colorHex : "#FFFFFF"));

        Label nameLabel = new Label(sectionName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setPrefWidth(200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label tasksLabel = new Label(tasksCompleted + " Tasks Done");
        tasksLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 14px; -fx-font-weight: bold;");
        tasksLabel.setPrefWidth(120);

        long h = timeSecs / 3600;
        long m = (timeSecs % 3600) / 60;
        Label timeLabel = new Label(String.format("⏱ %dh %dm", h, m));
        timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-size: 14px; -fx-font-weight: bold;");
        timeLabel.setPrefWidth(100);

        row.getChildren().addAll(colorRect, nameLabel, spacer, tasksLabel, timeLabel);
        return row;
    }
}