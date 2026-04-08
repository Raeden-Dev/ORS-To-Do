package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.Debuff;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDateTime;

public class DebuffCard extends VBox {

    public DebuffCard(Debuff debuff, AppStats appStats, Runnable onUpdate) {
        super(5);
        setStyle("-fx-background-color: " + debuff.getColorHex() + "33; -fx-border-color: " + debuff.getColorHex() + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");
        setPrefWidth(220);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(debuff.getIconSymbol());
        iconLbl.setStyle("-fx-text-fill: " + debuff.getColorHex() + "; -fx-font-size: 16px;");

        Label nameLbl = new Label(debuff.getName());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        header.getChildren().addAll(iconLbl, nameLbl);
        getChildren().add(header);

        if (debuff.getDescription() != null && !debuff.getDescription().isEmpty()) {
            Label descLbl = new Label(debuff.getDescription());
            descLbl.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 11px;");
            descLbl.setWrapText(true);
            getChildren().add(descLbl);
        }

        if (debuff.getRequiredTaskCompletions() > 0) {
            Label reqLbl = new Label("Tasks: " + debuff.getCurrentTaskCompletions() + " / " + debuff.getRequiredTaskCompletions());
            reqLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-font-weight: bold;");
            getChildren().add(reqLbl);
        }

        if (debuff.getExpiryDate() != null) {
            Duration d = Duration.between(LocalDateTime.now(), debuff.getExpiryDate());
            long hours = d.toHours();
            Label timeLbl = new Label("Expires in: " + (hours > 0 ? hours + "h" : d.toMinutes() + "m"));
            timeLbl.setStyle("-fx-text-fill: #569CD6; -fx-font-size: 11px; -fx-font-weight: bold;");
            getChildren().add(timeLbl);
        }

        Tooltip t = new Tooltip();
        StringBuilder sb = new StringBuilder("Effects:\n");
        appStats.getCustomStats().forEach(s -> {
            if (debuff.getStatGainMultipliers().containsKey(s.getId())) {
                sb.append("• ").append(s.getName()).append(" XP gain reduced to ").append(Math.round(debuff.getStatGainMultipliers().get(s.getId()) * 100)).append("%\n");
            }
            if (debuff.getStatCapReductions().containsKey(s.getId())) {
                sb.append("• ").append(s.getName()).append(" Max Cap lowered by ").append(debuff.getStatCapReductions().get(s.getId())).append("\n");
            }
        });
        t.setText(sb.toString().trim());
        Tooltip.install(this, t);

        // --- FIXED: Right-Click Removal Confirmation Dialog ---
        this.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to manually remove the '" + debuff.getName() + "' debuff early?", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Remove Debuff");
                TaskDialogs.styleDialog(confirm);

                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        appStats.getActiveDebuffs().remove(debuff);
                        StorageManager.saveStats(appStats);
                        onUpdate.run();
                    }
                });
            }
        });
    }
}