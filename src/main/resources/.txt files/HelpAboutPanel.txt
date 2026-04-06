package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelpAboutPanel extends VBox {

    public HelpAboutPanel(AppStats appStats) {
        super(15);
        // Container wrapper styling matching the rest of the settings panels
        setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label header = new Label("Help & About");
        header.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 16px; -fx-font-weight: bold;");

        // --- Cyan Help Button ---
        Button helpBtn = new Button("Help");
        helpBtn.setMaxWidth(Double.MAX_VALUE); // Stretches button to the end of the container
        String helpDefault = "-fx-background-color: #002222; -fx-border-color: #00FFFF; -fx-text-fill: #00FFFF; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 4 5; -fx-font-size: 14px;";
        String helpHover = "-fx-background-color: #004444; -fx-border-color: #00FFFF; -fx-text-fill: #00FFFF; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 4 5; -fx-font-size: 14px;";
        helpBtn.setStyle(helpDefault);
        helpBtn.setOnMouseEntered(e -> helpBtn.setStyle(helpHover));
        helpBtn.setOnMouseExited(e -> helpBtn.setStyle(helpDefault));
        helpBtn.setOnAction(e -> TaskDialogs.showHelpDialog(appStats));

        // --- Lime Credits Button ---
        Button creditsBtn = new Button("Credits");
        creditsBtn.setMaxWidth(Double.MAX_VALUE); // Stretches button to the end of the container
        String creditsDefault = "-fx-background-color: #0d260d; -fx-border-color: #32CD32; -fx-text-fill: #32CD32; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 4 5; -fx-font-size: 14px;";
        String creditsHover = "-fx-background-color: #1a4d1a; -fx-border-color: #32CD32; -fx-text-fill: #32CD32; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 4 5; -fx-font-size: 14px;";
        creditsBtn.setStyle(creditsDefault);
        creditsBtn.setOnMouseEntered(e -> creditsBtn.setStyle(creditsHover));
        creditsBtn.setOnMouseExited(e -> creditsBtn.setStyle(creditsDefault));
        creditsBtn.setOnAction(e -> TaskDialogs.showCreditsDialog());

        // Add them directly to the VBox so they stack in rows
        getChildren().addAll(header, helpBtn, creditsBtn);
    }
}