package com.raeden.ors_to_do.modules.dependencies.ui.dialogs;

public class ThemeConstants {

    // CSS for the main Dialogs (TextFields, Combos, Buttons, Checkboxes, etc.)
    public static final String DIALOG_BASE_CSS =
            ".dialog-pane { -fx-background-color: #1E1E1E; -fx-border-color: #3E3E42; -fx-border-width: 1; } " +
                    ".dialog-pane > *.content.label { -fx-text-fill: #E0E0E0; } " +
                    ".dialog-pane .header-panel { -fx-background-color: #2D2D30; -fx-border-bottom-color: #3E3E42; -fx-border-width: 0 0 1 0; } " +
                    ".dialog-pane .header-panel .label { -fx-text-fill: #569CD6; -fx-font-weight: bold; } " +
                    ".button { -fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; } " +
                    ".button:hover { -fx-background-color: #569CD6; -fx-border-color: #569CD6; } " +
                    ".button:default { -fx-background-color: #0E639C; -fx-border-color: #0E639C; } " +
                    ".button:default:hover { -fx-background-color: #1177BB; } " +
                    ".text-field, .text-area, .combo-box { -fx-background-color: #2D2D30; -fx-control-inner-background: #2D2D30; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; } " +
                    ".text-area .content { -fx-background-color: #2D2D30; } " +
                    ".combo-box .list-cell { -fx-text-fill: white; } " +
                    ".combo-box-popup .list-view { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".combo-box-popup .list-view .list-cell { -fx-background-color: #2D2D30; -fx-text-fill: white; } " +
                    ".combo-box-popup .list-view .list-cell:filled:hover, .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color: #569CD6; -fx-text-fill: white; } " +
                    ".color-picker { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".color-picker .label { -fx-text-fill: white; } " +
                    ".label, .check-box { -fx-text-fill: #E0E0E0; } " +
                    ".check-box .box { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".check-box:selected .mark { -fx-background-color: white; } " +
                    ".custom-menu-btn { -fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; } " +
                    ".custom-menu-btn .label { -fx-text-fill: white; } " +
                    ".context-menu { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".menu-item { -fx-background-color: #2D2D30; } " +
                    ".menu-item:hover, .menu-item:focused { -fx-background-color: #569CD6; } " +
                    ".menu-item .label { -fx-text-fill: white; }";

    // CSS specifically for priority Dropdowns
    public static final String PRIORITY_COMBO_CSS =
            ".combo-box { -fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand; } " +
                    ".combo-box .list-cell { -fx-background-color: transparent; } " +
                    ".combo-box-popup .list-view { -fx-background-color: #2D2D30; -fx-border-color: #555555; } " +
                    ".combo-box-popup .list-view .list-cell { -fx-background-color: #2D2D30; -fx-text-fill: white; } " +
                    ".combo-box-popup .list-view .list-cell:filled:hover, .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color: #3E3E42; } " +
                    ".combo-box .arrow-button { -fx-background-color: transparent; } " +
                    ".combo-box .arrow { -fx-background-color: #AAAAAA; }";

    // Transparent scroll pane CSS for the Help menu
    public static final String TRANSPARENT_SCROLL_CSS =
            ".scroll-pane > .viewport { -fx-background-color: transparent; } .scroll-bar:vertical { -fx-opacity: 0.7; }";
}