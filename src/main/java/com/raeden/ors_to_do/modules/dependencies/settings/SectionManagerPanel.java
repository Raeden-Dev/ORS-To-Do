package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

public class SectionManagerPanel extends VBox {

    private VBox existingSectionsBox;
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;
    private Runnable onSectionChanged;

    public SectionManagerPanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback, Runnable onSectionChanged) {
        super(15);
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;
        this.onSectionChanged = onSectionChanged;

        setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label textHeader = new Label("Manage Dynamic Sections");
        textHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button addSectionBtn = new Button("+ Add Section");
        addSectionBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;");
        addSectionBtn.setPrefWidth(200);

        // --- NEW: Add Separator Button ---
        Button addSeparatorBtn = new Button("+ Add Separator");
        addSeparatorBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;");
        addSeparatorBtn.setPrefWidth(200);

        topRow.getChildren().addAll(addSectionBtn, addSeparatorBtn);

        existingSectionsBox = new VBox(10);

        addSectionBtn.setOnAction(e -> {
            SectionConfig newConfig = new SectionConfig(UUID.randomUUID().toString(), "");
            SectionEditDialog.show(newConfig, true, appStats, () -> {
                StorageManager.saveStats(appStats);
                refreshList();
                if (this.onSectionChanged != null) this.onSectionChanged.run();
                this.refreshCallback.run();
            });
        });

        // --- NEW: Create Separator Logic ---
        addSeparatorBtn.setOnAction(e -> {
            TextInputDialog nameDialog = new TextInputDialog("");
            nameDialog.setTitle("Add Separator");
            nameDialog.setHeaderText("Enter a label for this category separator\n(Leave blank for just a line):");
            TaskDialogs.styleDialog(nameDialog);

            nameDialog.showAndWait().ifPresent(name -> {
                SectionConfig sepConfig = new SectionConfig(UUID.randomUUID().toString(), name.trim());
                sepConfig.setSeparator(true); // Flag it as a separator!
                appStats.getSections().add(sepConfig);
                StorageManager.saveStats(appStats);
                refreshList();
                if (this.onSectionChanged != null) this.onSectionChanged.run();
                this.refreshCallback.run();
            });
        });

        getChildren().addAll(textHeader, topRow, new Separator(), existingSectionsBox);
        refreshList();
    }

    private void refreshList() {
        existingSectionsBox.getChildren().clear();
        for (int i = 0; i < appStats.getSections().size(); i++) {
            SectionConfig config = appStats.getSections().get(i);
            SectionRow row = new SectionRow(config, i, appStats, globalDatabase, this::refreshList, onSectionChanged, refreshCallback);
            existingSectionsBox.getChildren().add(row);
        }
    }
}