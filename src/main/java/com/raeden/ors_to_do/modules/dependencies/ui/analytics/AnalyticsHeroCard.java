package com.raeden.ors_to_do.modules.dependencies.ui.analytics;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AnalyticsHeroCard extends VBox {

    protected Label titleLabel;
    protected Label valLabel;
    protected Label subLabel;

    public AnalyticsHeroCard(String title, String value, String colorHex) {
        this(title, value, null, colorHex);
    }

    public AnalyticsHeroCard(String title, String value, String subtitle, String colorHex) {
        super(5);
        setAlignment(Pos.CENTER);

        // --- FIXED: Shrunk the base card size ---
        setPrefSize(170, 95);
        setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px; -fx-font-weight: bold;");

        valLabel = new Label(value);
        valLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 26px; -fx-font-weight: bold;");

        getChildren().addAll(titleLabel, valLabel);

        subLabel = new Label(subtitle != null ? subtitle : "");
        subLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 10px; -fx-font-style: italic;");
        if (subtitle != null && !subtitle.isEmpty()) {
            getChildren().add(subLabel);
        }
    }

    public void setValue(String value) { valLabel.setText(value); }
    public void setSubtitle(String subtitle) {
        subLabel.setText(subtitle);
        if (!getChildren().contains(subLabel)) getChildren().add(subLabel);
    }
}