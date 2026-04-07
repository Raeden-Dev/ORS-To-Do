package com.raeden.ors_to_do.modules.dependencies.ui.layout;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class UrgeSurfingOverlay extends StackPane {
    private Timeline countdownTimeline;
    private ScaleTransition breatheAnimation;
    private int secondsRemaining;

    public UrgeSurfingOverlay(AppStats appStats, Runnable onClose) {
        setStyle("-fx-background-color: #121212;"); // Dark background

        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.CENTER);

        // --- 1. Top Quote ---
        List<String> quotes = appStats.getUrgeQuotes();
        String randomQuote = quotes.isEmpty() ? "Breathe through the urge." : quotes.get(new Random().nextInt(quotes.size()));

        Label quoteLabel = new Label("\"" + randomQuote + "\"");
        quoteLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 20px; -fx-font-style: italic; -fx-text-alignment: center;");
        quoteLabel.setWrapText(true);
        quoteLabel.setMaxWidth(600);

        // --- 2. Breathing Circle ---
        Circle breatheCircle = new Circle(50, Color.web("#569CD6", 0.5));
        breatheCircle.setStroke(Color.web("#569CD6"));
        breatheCircle.setStrokeWidth(2);

        Label instructionLabel = new Label("Get Ready...");
        instructionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        StackPane circlePane = new StackPane(breatheCircle, instructionLabel);
        circlePane.setPrefHeight(300); // Give it room to expand

        // --- 3. Countdown Timer ---
        secondsRemaining = appStats.getUrgeSessionDurationSeconds();
        Label timerLabel = new Label(formatTime(secondsRemaining));
        timerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        // --- 4. Controls ---
        Button cancelBtn = new Button("Give Up (Cancel)");
        cancelBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20;");

        cancelBtn.setOnAction(e -> {
            stopAnimations();
            onClose.run();
        });

        contentBox.getChildren().addAll(quoteLabel, circlePane, timerLabel, cancelBtn);
        getChildren().add(contentBox);

        // --- Setup Breathing Animation (Box Breathing: Inhale 4s, Hold 4s, Exhale 4s, Hold 4s) ---
        breatheAnimation = new ScaleTransition(Duration.seconds(4), breatheCircle);

        Timeline instructionTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    instructionLabel.setText("Breathe In...");
                    breatheAnimation.setByX(2.0f); // Scale to 3x
                    breatheAnimation.setByY(2.0f);
                    breatheAnimation.playFromStart();
                }),
                new KeyFrame(Duration.seconds(4), e -> instructionLabel.setText("Hold...")),
                new KeyFrame(Duration.seconds(8), e -> {
                    instructionLabel.setText("Breathe Out...");
                    breatheAnimation.setByX(-2.0f); // Scale back to 1x
                    breatheAnimation.setByY(-2.0f);
                    breatheAnimation.playFromStart();
                }),
                new KeyFrame(Duration.seconds(12), e -> instructionLabel.setText("Hold..."))
        );
        instructionTimeline.setCycleCount(Animation.INDEFINITE);

        // --- Setup Countdown ---
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            timerLabel.setText(formatTime(secondsRemaining));

            if (secondsRemaining <= 0) {
                stopAnimations();
                instructionLabel.setText("Urge Survived.");
                cancelBtn.setText("Continue");
                cancelBtn.setStyle("-fx-background-color: #4EC9B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20;");
                breatheCircle.setScaleX(1.0);
                breatheCircle.setScaleY(1.0);
            }
        }));
        countdownTimeline.setCycleCount(Animation.INDEFINITE);

        // Start
        instructionTimeline.play();
        countdownTimeline.play();
    }

    private void stopAnimations() {
        if (countdownTimeline != null) countdownTimeline.stop();
        if (breatheAnimation != null) breatheAnimation.stop();
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}