package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.settings.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import java.util.List;

public class SettingsModule extends ScrollPane {

    public SettingsModule(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        setBorder(Border.EMPTY);

        VBox contentBox = new VBox(20); // Changed spacing to 20 to match your design goals
        contentBox.setPadding(new Insets(20));

        // --- NEW: Help & About Panel ---
        HelpAboutPanel helpPanel = new HelpAboutPanel(appStats);

        // --- Existing Control Center ---
        Label header = new Label("Control Center");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        GeneralSettingsPanel generalPanel = new GeneralSettingsPanel(appStats, refreshCallback);
        TemplateManagerPanel templatePanel = new TemplateManagerPanel(appStats, globalDatabase, refreshCallback);
        PriorityManagerPanel priorityPanel = new PriorityManagerPanel(appStats, refreshCallback);
        DataManagementPanel dataPanel = new DataManagementPanel(appStats, globalDatabase, refreshCallback);
        DangerZonePanel dangerPanel = new DangerZonePanel(appStats, globalDatabase, refreshCallback);
        StatsManagerPanel statsManagerPanel = new StatsManagerPanel(appStats, refreshCallback);

        Runnable onSectionChanged = () -> {
            templatePanel.refreshSectionSelector();
            dangerPanel.refreshDangerZone();
            refreshCallback.run();
        };
        SectionManagerPanel sectionPanel = new SectionManagerPanel(appStats, globalDatabase, refreshCallback, onSectionChanged);

        // Add to view
        contentBox.getChildren().addAll(
                helpPanel,
                header,
                sectionPanel,
                generalPanel,
                templatePanel,
                statsManagerPanel,
                priorityPanel,
                dataPanel,
                dangerPanel
        );

        setContent(contentBox);
    }
}