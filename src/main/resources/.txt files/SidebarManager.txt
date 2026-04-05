package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

// --- FIXED: Upgraded from VBox to BorderPane for strict Top/Center/Bottom layout pinning ---
public class SidebarManager extends BorderPane {
    private AppStats appStats;
    private GlobalSearchBar searchBar;
    private Consumer<String> onNavigate;
    private String currentActiveModule = "QUICK";
    private List<TaskItem> globalDatabase;

    private boolean isStaticExpanded = true;

    public SidebarManager(AppStats appStats, List<TaskItem> globalDatabase, GlobalSearchBar searchBar, Consumer<String> onNavigate) {
        this.appStats = appStats;
        this.searchBar = searchBar;
        this.onNavigate = onNavigate;
        this.globalDatabase = globalDatabase;

        getStyleClass().add("sidebar");
        setPrefWidth(220);

        refreshSidebar();
    }

    public void refreshSidebar() {
        setTop(null);
        setCenter(null);
        setBottom(null);

        // ==========================================
        // 1. TOP REGION: Search Bar
        // ==========================================
        VBox topBox = new VBox();
        topBox.setPadding(new Insets(0, 0, 10, 0));
        topBox.getChildren().add(searchBar);
        setTop(topBox);

        // ==========================================
        // 2. CENTER REGION: Scrollable Dynamic Sections
        // ==========================================
        VBox dynamicSectionsBox = new VBox();
        dynamicSectionsBox.setStyle("-fx-background-color: transparent;");

        for (SectionConfig config : appStats.getSections()) {
            int activeTaskCount = 0;

            if (appStats.isShowSidebarTaskCount() && globalDatabase != null) {
                for (TaskItem task : globalDatabase) {
                    if (config.getId().equals(task.getSectionId()) && !task.isFinished() && !task.isArchived()) {
                        activeTaskCount++;
                    }
                }
            }

            dynamicSectionsBox.getChildren().add(createSidebarButton(config.getName(), config.getId(), config.getSidebarColor(), activeTaskCount));
        }

        ScrollPane scrollPane = new ScrollPane(dynamicSectionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        String scrollCss = ".scroll-pane { -fx-background-color: transparent; -fx-padding: 0; } " +
                ".scroll-pane > .viewport { -fx-background-color: transparent; } " +
                ".scroll-bar:vertical { -fx-background-color: transparent; -fx-pref-width: 5; } " +
                ".scroll-bar:vertical .thumb { -fx-background-color: #3E3E42; -fx-background-radius: 5; } " +
                ".scroll-bar:vertical .thumb:hover { -fx-background-color: #555555; }";
        scrollPane.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(scrollCss.getBytes()));

        setCenter(scrollPane);

        // ==========================================
        // 3. BOTTOM REGION: Pinned Static Modules
        // ==========================================
        VBox bottomBox = new VBox();
        bottomBox.setStyle("-fx-background-color: transparent;");

        // --- Interactive Separator ---
        VBox separatorArea = new VBox(2);
        separatorArea.setAlignment(Pos.CENTER);
        separatorArea.setCursor(Cursor.HAND);
        separatorArea.setStyle("-fx-background-color: transparent;");

        Label arrowLabel = new Label(isStaticExpanded ? "▼" : "▲");
        arrowLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 10px;");
        arrowLabel.setOpacity(0.0);

        Separator sep = new Separator();
        sep.setPadding(new Insets(2, 0, 8, 0));

        separatorArea.getChildren().addAll(arrowLabel, sep);
        separatorArea.setOnMouseEntered(e -> arrowLabel.setOpacity(1.0));
        separatorArea.setOnMouseExited(e -> arrowLabel.setOpacity(0.0));

        // --- Animated Static Modules Container ---
        VBox staticModulesBox = new VBox();
        staticModulesBox.setMinHeight(0);

        staticModulesBox.getChildren().addAll(
                createSidebarButton(appStats.getNavFocusText(), "FOCUS", appStats.getNavFocusColor(), -1),
                createSidebarButton(appStats.getNavAnalyticsText(), "ANALYTICS", appStats.getNavAnalyticsColor(), -1),
                createSidebarButton(appStats.getNavArchiveText(), "ARCHIVE", appStats.getNavArchiveColor(), -1),
                createSidebarButton(appStats.getNavSettingsText(), "SETTINGS", appStats.getNavSettingsColor(), -1)
        );

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(staticModulesBox.widthProperty());
        clipRect.heightProperty().bind(staticModulesBox.maxHeightProperty());
        staticModulesBox.setClip(clipRect);

        if (!isStaticExpanded) {
            staticModulesBox.setMaxHeight(0);
            staticModulesBox.setOpacity(0);
            staticModulesBox.setManaged(false);
            staticModulesBox.setVisible(false);
        } else {
            staticModulesBox.setMaxHeight(200);
            staticModulesBox.setOpacity(1);
        }

        // Toggle Animation Logic
        separatorArea.setOnMouseClicked(e -> {
            isStaticExpanded = !isStaticExpanded;
            arrowLabel.setText(isStaticExpanded ? "▼" : "▲");

            if (isStaticExpanded) {
                staticModulesBox.setManaged(true);
                staticModulesBox.setVisible(true);

                Timeline tl = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(staticModulesBox.maxHeightProperty(), 0),
                                new KeyValue(staticModulesBox.opacityProperty(), 0)
                        ),
                        new KeyFrame(Duration.millis(250),
                                new KeyValue(staticModulesBox.maxHeightProperty(), 200),
                                new KeyValue(staticModulesBox.opacityProperty(), 1)
                        )
                );
                tl.play();
            } else {
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(staticModulesBox.maxHeightProperty(), staticModulesBox.getHeight()),
                                new KeyValue(staticModulesBox.opacityProperty(), 1)
                        ),
                        new KeyFrame(Duration.millis(250),
                                new KeyValue(staticModulesBox.maxHeightProperty(), 0),
                                new KeyValue(staticModulesBox.opacityProperty(), 0)
                        )
                );
                tl.setOnFinished(evt -> {
                    staticModulesBox.setManaged(false);
                    staticModulesBox.setVisible(false);
                });
                tl.play();
            }
        });

        bottomBox.getChildren().addAll(separatorArea, staticModulesBox);
        setBottom(bottomBox);
    }

    private Button createSidebarButton(String displayText, String internalId, String hexColor, int taskCount) {
        Button btn = new Button(displayText);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        Rectangle rect = new Rectangle(5, 20);
        rect.setArcWidth(3); rect.setArcHeight(3);
        rect.setFill(Color.web(hexColor != null ? hexColor : "#FFFFFF"));

        HBox graphicContainer = new HBox(8);
        graphicContainer.setAlignment(Pos.CENTER_LEFT);

        if (taskCount > 0 && appStats.isShowSidebarTaskCount()) {
            String displayCount = String.format("%02d", taskCount);
            Label countLabel = new Label(displayCount);
            countLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px; -fx-font-weight: bold;");
            graphicContainer.getChildren().add(countLabel);
        }

        graphicContainer.getChildren().add(rect);

        btn.setGraphic(graphicContainer);
        btn.setGraphicTextGap(10);

        if (currentActiveModule.equals(internalId)) {
            btn.getStyleClass().add("active");
        }

        btn.setOnAction(e -> onNavigate.accept(internalId));

        return btn;
    }

    public void setActiveModule(String internalId) {
        this.currentActiveModule = internalId;
        refreshSidebar();
    }

    public String getActiveModule() {
        return currentActiveModule;
    }
}