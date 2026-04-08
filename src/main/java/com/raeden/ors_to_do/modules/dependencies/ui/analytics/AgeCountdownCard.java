package com.raeden.ors_to_do.modules.dependencies.ui.analytics;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AgeCountdownCard extends AnalyticsHeroCard {
    private AppStats appStats;
    private Timeline timeline;

    public AgeCountdownCard(AppStats appStats, Runnable onSave) {
        super("⏳ Life & Time", "You are 0 years old", "Double-click to set up", "#FF6666");
        this.appStats = appStats;

        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(12, 20, 12, 20));
        setStyle("-fx-background-color: #331A1A; -fx-border-color: #FF6666; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        titleLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-font-weight: bold;");
        valLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 26px; -fx-font-weight: bold;");
        subLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px; -fx-font-style: italic;");

        updateTick();
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateTick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showSetupDialog(onSave);
            }
        });

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) timeline.stop();
        });
    }

    private void updateTick() {
        LocalDate dob = appStats.getUserBirthDate();
        if (dob == null) {
            setValue("Not Set");
            setSubtitle("Double-click to set up");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime birthTime = dob.atStartOfDay();

        long millisLived = ChronoUnit.MILLIS.between(birthTime, now);
        double yearsLived = millisLived / (1000.0 * 60 * 60 * 24 * 365.2425);
        int currentAge = (int) yearsLived;

        // --- FIXED: Cast the float to an integer so it just shows the whole number ---
        setValue(String.format("You are %d years old", currentAge));

        LocalDateTime targetTime = birthTime.plusYears(appStats.getUserTargetAge());
        long millisLeft = ChronoUnit.MILLIS.between(now, targetTime);

        if (millisLeft < 0) {
            setSubtitle("Target reached! Keep going.");
        } else {
            long daysLeft = millisLeft / (1000L * 60 * 60 * 24);
            long hoursLeft = (millisLeft / (1000L * 60 * 60)) % 24;
            long minutesLeft = (millisLeft / (1000L * 60)) % 60;
            long secondsLeft = (millisLeft / 1000L) % 60;

            setSubtitle(String.format("You have %d days, %d hours, %d minutes, %d seconds until you are %d years old",
                    daysLeft, hoursLeft, minutesLeft, secondsLeft, appStats.getUserTargetAge()));
        }
    }

    private void showSetupDialog(Runnable onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Configure Life Stats");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        DatePicker dobPicker = new DatePicker(appStats.getUserBirthDate());
        dobPicker.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> targetAgeSpinner = new Spinner<>(1, 150, appStats.getUserTargetAge());
        targetAgeSpinner.setEditable(true);
        targetAgeSpinner.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(
                new Label("Your Birth Date:"), dobPicker,
                new Label("Target Age:"), targetAgeSpinner
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                appStats.setUserBirthDate(dobPicker.getValue());
                appStats.setUserTargetAge(targetAgeSpinner.getValue());
                StorageManager.saveStats(appStats);
                onSave.run();
            }
        });
    }
}