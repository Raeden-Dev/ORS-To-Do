package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.settings.*;
import com.raeden.ors_to_do.modules.dependencies.settings.DataManagementPanel;
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

        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(20));

        Label header = new Label("Control Center");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Instantiate the isolated UI Components
        GeneralSettingsPanel generalPanel = new GeneralSettingsPanel(appStats, refreshCallback);
        TemplateManagerPanel templatePanel = new TemplateManagerPanel(appStats, refreshCallback);
        PriorityManagerPanel priorityPanel = new PriorityManagerPanel(appStats, refreshCallback);
        DataManagementPanel dataPanel = new DataManagementPanel(appStats, globalDatabase, refreshCallback); // --- NEW
        DangerZonePanel dangerPanel = new DangerZonePanel(appStats, globalDatabase, refreshCallback);

        // A callback to update Template Dropdowns and Danger Zone lists when a Section is created/deleted
        Runnable onSectionChanged = () -> {
            templatePanel.refreshSectionSelector();
            dangerPanel.refreshDangerZone();
            refreshCallback.run();
        };

        SectionManagerPanel sectionPanel = new SectionManagerPanel(appStats, globalDatabase, refreshCallback, onSectionChanged);

        // Assemble them into the UI (Data panel sitting right above Danger Zone)
        contentBox.getChildren().addAll(
                header,
                sectionPanel,
                generalPanel,
                templatePanel,
                priorityPanel,
                dataPanel,
                dangerPanel
        );

        setContent(contentBox);
    }
}