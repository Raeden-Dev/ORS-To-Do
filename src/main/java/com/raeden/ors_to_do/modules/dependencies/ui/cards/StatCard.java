package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StatCard extends VBox {

    public StatCard(CustomStat stat, AppStats appStats, Runnable onUpdate) {
        super();

        // Dynamic Styling from Config
        String bgColor = stat.getBackgroundColor() != null && !stat.getBackgroundColor().equals("transparent") ? stat.getBackgroundColor() : "#2D2D30";
        String txtColor = stat.getTextColor() != null && !stat.getTextColor().equals("transparent") ? stat.getTextColor() : "#FFFFFF";

        // Main Row Container mimicking TaskCard
        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10, 15, 10, 10));

        // Apply background and border styling
        String defaultStyle = "-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: " + txtColor + "66; -fx-border-width: 1;";
        String hoverStyle = "-fx-background-color: derive(" + bgColor + ", 10%); -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: " + txtColor + "; -fx-border-width: 1;";
        mainRow.setStyle(defaultStyle);

        // 1. Icon
        if (stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") && !stat.getIconSymbol().isEmpty()) {
            Label iconLabel = new Label(stat.getIconSymbol());
            iconLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 14px;");
            mainRow.getChildren().add(iconLabel);
        }

        // 2. Left Side Color Bar
        Rectangle sideBar = new Rectangle(4, 20, Color.web(txtColor));
        sideBar.setArcWidth(3);
        sideBar.setArcHeight(3);
        mainRow.getChildren().add(sideBar);

        // 3. Name
        Label nameLabel = new Label(stat.getName());
        nameLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        mainRow.getChildren().add(nameLabel);

        // 4. Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        mainRow.getChildren().add(spacer);

        // 5. Help "?" Button
        Button helpBtn = new Button("?");
        helpBtn.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: " + txtColor + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: " + txtColor + "88; -fx-border-radius: 50; -fx-background-radius: 50; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-padding: 0;");
        helpBtn.setOnAction(e -> showStatDescriptionDialog(stat, txtColor, bgColor));

        // 6. Right Side: Amount / Max
        Label amountLabel = new Label(String.valueOf(stat.getCurrentAmount()));
        amountLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label maxLabel = new Label("/ " + stat.getMaxCap());
        maxLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 12px; -fx-padding: 3 0 0 0;");

        mainRow.getChildren().addAll(helpBtn, amountLabel, maxLabel);

        getChildren().add(mainRow);

        // Subtle hover effect
        mainRow.setOnMouseEntered(e -> mainRow.setStyle(hoverStyle));
        mainRow.setOnMouseExited(e -> mainRow.setStyle(defaultStyle));
    }

    private void showStatDescriptionDialog(CustomStat stat, String txtColor, String bgColor) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Stat Information");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + txtColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        content.setPrefWidth(300);

        Label title = new Label((stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "") + stat.getName());
        title.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label desc = new Label(stat.getDescription() == null || stat.getDescription().isEmpty() ? "No lore or description provided for this stat." : stat.getDescription());
        desc.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px; -fx-font-style: italic; -fx-text-alignment: center;");
        desc.setWrapText(true);

        Label values = new Label("Current: " + stat.getCurrentAmount() + "  |  Max: " + stat.getMaxCap());
        values.setStyle("-fx-text-fill: #858585; -fx-font-size: 12px; -fx-font-weight: bold;");

        content.getChildren().addAll(title, new Separator(), desc, new Separator(), values);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }
}