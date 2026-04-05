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

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));

        HelpAboutPanel helpPanel = new HelpAboutPanel(appStats);

        Label header = new Label("Control Center");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        StatsManagerPanel statsManagerPanel = new StatsManagerPanel(appStats, refreshCallback);

        Runnable wrappedRefreshCallback = () -> {
            // --- FIXED: Call the internal refresh method instead of overriding ---
            statsManagerPanel.refreshState();
            refreshCallback.run();
        };

        GeneralSettingsPanel generalPanel = new GeneralSettingsPanel(appStats, wrappedRefreshCallback);
        TemplateManagerPanel templatePanel = new TemplateManagerPanel(appStats, globalDatabase, wrappedRefreshCallback);
        PriorityManagerPanel priorityPanel = new PriorityManagerPanel(appStats, wrappedRefreshCallback);
        DataManagementPanel dataPanel = new DataManagementPanel(appStats, globalDatabase, wrappedRefreshCallback);
        DangerZonePanel dangerPanel = new DangerZonePanel(appStats, globalDatabase, wrappedRefreshCallback);

        Runnable onSectionChanged = () -> {
            templatePanel.refreshSectionSelector();
            dangerPanel.refreshDangerZone();
            wrappedRefreshCallback.run();
        };
        SectionManagerPanel sectionPanel = new SectionManagerPanel(appStats, globalDatabase, wrappedRefreshCallback, onSectionChanged);

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