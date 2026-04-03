package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

public class SidebarManager extends VBox {
    private AppStats appStats;
    private GlobalSearchBar searchBar;
    private Consumer<String> onNavigate;
    private String currentActiveModule = "QUICK";

    public SidebarManager(AppStats appStats, GlobalSearchBar searchBar, Consumer<String> onNavigate) {
        this.appStats = appStats;
        this.searchBar = searchBar;
        this.onNavigate = onNavigate;

        getStyleClass().add("sidebar");
        setPrefWidth(220);

        refreshSidebar();
    }

    public void refreshSidebar() {
        getChildren().clear();

        // Add Search Bar
        VBox.setMargin(searchBar, new Insets(0, 0, 10, 0));
        getChildren().add(searchBar);

        // Render Dynamic Sections
        for (SectionConfig config : appStats.getSections()) {
            addSidebarButton(config.getName(), config.getId(), config.getSidebarColor());
        }

        // Add spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // Add Separator
        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));
        getChildren().add(sep);

        // Render Static Modules
        addSidebarButton(appStats.getNavFocusText(), "FOCUS", appStats.getNavFocusColor());
        addSidebarButton(appStats.getNavAnalyticsText(), "ANALYTICS", appStats.getNavAnalyticsColor());
        addSidebarButton(appStats.getNavArchiveText(), "ARCHIVE", appStats.getNavArchiveColor());
        addSidebarButton(appStats.getNavSettingsText(), "SETTINGS", appStats.getNavSettingsColor());
    }

    private void addSidebarButton(String displayText, String internalId, String hexColor) {
        Button btn = new Button(displayText);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        Rectangle rect = new Rectangle(5, 20);
        rect.setArcWidth(3); rect.setArcHeight(3);
        rect.setFill(Color.web(hexColor != null ? hexColor : "#FFFFFF"));

        btn.setGraphic(rect);
        btn.setGraphicTextGap(10);

        if (currentActiveModule.equals(internalId)) {
            btn.getStyleClass().add("active");
        }

        btn.setOnAction(e -> onNavigate.accept(internalId));

        getChildren().add(btn);
    }

    public void setActiveModule(String internalId) {
        this.currentActiveModule = internalId;
        refreshSidebar(); // Visually update active styling
    }

    public String getActiveModule() {
        return currentActiveModule;
    }
}