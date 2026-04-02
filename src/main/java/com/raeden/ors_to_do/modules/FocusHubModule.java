package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.PomodoroTimer;
import com.raeden.ors_to_do.modules.dependencies.Scratchpad;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import java.util.List;

public class FocusHubModule extends HBox {
    private PomodoroTimer timerComponent;
    private Scratchpad scratchpadComponent;

    public FocusHubModule(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        setSpacing(20);
        setPadding(new Insets(20));

        timerComponent = new PomodoroTimer(appStats, globalDatabase, refreshCallback);
        scratchpadComponent = new Scratchpad(appStats);

        getChildren().addAll(timerComponent, scratchpadComponent);
        refreshTasks();
    }

    public void refreshTasks() {
        if (timerComponent != null) {
            timerComponent.refreshTasks();
        }
    }
}