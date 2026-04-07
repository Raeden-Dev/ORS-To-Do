package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.layout.PomodoroTimer;
import com.raeden.ors_to_do.modules.dependencies.ui.layout.Scratchpad;
import com.raeden.ors_to_do.modules.dependencies.ui.layout.UrgeSurfingOverlay;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

public class FocusHubModule extends StackPane {
    private PomodoroTimer timerComponent;
    private Scratchpad scratchpadComponent;
    private HBox mainLayout;
    private AppStats appStats;

    public FocusHubModule(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        this.appStats = appStats;

        mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));

        // Pass the trigger for the Urge Overlay into the Timer component
        timerComponent = new PomodoroTimer(appStats, globalDatabase, refreshCallback, this::showUrgeOverlay);
        scratchpadComponent = new Scratchpad(appStats);

        mainLayout.getChildren().addAll(timerComponent, scratchpadComponent);
        getChildren().add(mainLayout);

        refreshTasks();
    }

    public void refreshTasks() {
        if (timerComponent != null) {
            timerComponent.refreshTasks();
        }
    }

    // --- NEW: Overlay Launch Logic ---
    private void showUrgeOverlay() {
        UrgeSurfingOverlay overlay = new UrgeSurfingOverlay(appStats, () -> {
            // Callback for when the user clicks 'Continue' or 'Give Up'
            getChildren().removeIf(node -> node instanceof UrgeSurfingOverlay);
            mainLayout.setVisible(true);
        });

        // Hide the main dashboard and slap the breathing animation on top
        mainLayout.setVisible(false);
        getChildren().add(overlay);
    }
}