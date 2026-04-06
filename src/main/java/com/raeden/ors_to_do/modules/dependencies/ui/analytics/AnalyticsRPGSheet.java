package com.raeden.ors_to_do.modules.dependencies.ui.analytics;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AnalyticsRPGSheet extends VBox {

    public AnalyticsRPGSheet(AppStats appStats) {
        super(15);
        setStyle("-fx-background-color: #252526; -fx-padding: 20; -fx-background-radius: 8; -fx-border-color: #3E3E42; -fx-border-radius: 8;");

        Label title = new Label("RPG Character Sheet");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 20px; -fx-font-weight: bold;");
        getChildren().add(title);

        if (appStats.getCustomStats().isEmpty()) {
            Label empty = new Label("No custom stats created yet. Go to General Settings to define your RPG stats.");
            empty.setStyle("-fx-text-fill: #AAAAAA; -fx-font-style: italic;");
            getChildren().add(empty);
            return;
        }

        FlowPane statsGrid = new FlowPane(15, 15);

        for (CustomStat stat : appStats.getCustomStats()) {
            VBox statCard = new VBox(10);
            String bgColor = stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333";
            String txtColor = stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF";
            statCard.setStyle("-fx-background-color: #2D2D30; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: " + bgColor + "; -fx-border-width: 2; -fx-border-radius: 5;");
            statCard.setPrefWidth(300);

            String icon = (stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None")) ? stat.getIconSymbol() + " " : "";
            Label nameLbl = new Label(icon + stat.getName());
            nameLbl.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");

            String capText = stat.getMaxCap() > 0 ? " / " + stat.getMaxCap() : "";
            Label amountLbl = new Label(stat.getCurrentAmount() + capText + " XP");
            amountLbl.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px; -fx-font-weight: bold;");

            HBox header = new HBox(nameLbl, new Region(), amountLbl);
            HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);

            ProgressBar pBar = new ProgressBar();
            pBar.setProgress(stat.getMaxCap() > 0 ? (double) stat.getCurrentAmount() / stat.getMaxCap() : 1.0);
            pBar.setPrefWidth(Double.MAX_VALUE);
            pBar.setStyle("-fx-accent: " + bgColor + "; -fx-control-inner-background: #1E1E1E; -fx-background-radius: 3;");

            HBox lifetimeBox = new HBox(15);
            Label earned = new Label("▲ " + stat.getLifetimeEarned() + " Earned");
            earned.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label lost = new Label("▼ " + stat.getLifetimeLost() + " Lost");
            lost.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label peak = new Label("⭐ Peak: " + stat.getMaxLevelReached());
            peak.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold;");
            lifetimeBox.getChildren().addAll(earned, lost, peak);

            statCard.getChildren().addAll(header, pBar, lifetimeBox);
            statsGrid.getChildren().add(statCard);
        }
        getChildren().add(statsGrid);
    }
}