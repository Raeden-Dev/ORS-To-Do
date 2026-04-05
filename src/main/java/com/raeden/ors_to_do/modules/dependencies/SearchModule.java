package com.raeden.ors_to_do.modules.dependencies;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskCard;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SearchModule extends BorderPane {
    private List<Timeline> activeTimelines = new ArrayList<>();

    // --- UPDATED: Accepts a Consumer<String> to tell the main app to switch pages ---
    public SearchModule(String query, List<TaskItem> globalDatabase, AppStats appStats, Runnable onUpdate, Consumer<String> onNavigate) {
        setPadding(new Insets(15));

        // --- Header ---
        VBox topArea = new VBox(10);
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label titleLabel = new Label("🔍 Search Results for: \"" + query + "\"");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4EC9B0;");
        headerBox.getChildren().add(titleLabel);

        topArea.getChildren().add(headerBox);
        setTop(topArea);

        // --- Content Area ---
        VBox listContainer = new VBox(8);
        // --- ADDED PADDING: 20px of space right above the first task card ---
        listContainer.setPadding(new Insets(20, 5, 10, 5));

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);
        setCenter(scrollPane);

        // --- Deep Search Logic ---
        String q = query.toLowerCase();
        List<TaskItem> results = new ArrayList<>();

        for (TaskItem task : globalDatabase) {
            boolean match = false;

            if (task.getTextContent().toLowerCase().contains(q)) match = true;
            else if (task.getTaskType() != null && task.getTaskType().toLowerCase().contains(q)) match = true;
            else if (task.getPrefix() != null && task.getPrefix().toLowerCase().contains(q)) match = true;
            else {
                for (SubTask sub : task.getSubTasks()) {
                    if (sub.getTextContent().toLowerCase().contains(q)) {
                        match = true;
                        break;
                    }
                }
            }

            if (match) results.add(task);
        }

        // --- Rendering ---
        if (results.isEmpty()) {
            Label emptyLabel = new Label("No tasks found matching your search.");
            emptyLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyLabel);
        } else {

            // --- NEW: Routing Logic to figure out exactly what page the task belongs to ---
            Consumer<TaskItem> handleGoToPage = (t) -> {
                if (t.isArchived()) {
                    onNavigate.accept("ARCHIVE");
                } else if (t.getSectionId() != null) {
                    onNavigate.accept(t.getSectionId());
                } else if (t.getLegacyOriginModule() != null) {
                    onNavigate.accept(t.getLegacyOriginModule().name());
                }
            };

            for (TaskItem task : results) {
                SectionConfig config = appStats.getSections().stream()
                        .filter(s -> s.getId().equals(task.getSectionId()))
                        .findFirst()
                        .orElse(new SectionConfig("legacy", "Legacy/Archived"));

                // Call the new overloaded constructor with `handleGoToPage`
                listContainer.getChildren().add(new TaskCard(
                        task, config, appStats, globalDatabase, onUpdate, activeTimelines, (a, b) -> {}, handleGoToPage
                ));
            }
        }
    }

    public void cleanupTimelines() {
        for (Timeline t : activeTimelines) t.stop();
        activeTimelines.clear();
    }
}