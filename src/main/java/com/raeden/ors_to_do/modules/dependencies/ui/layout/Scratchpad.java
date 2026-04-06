package com.raeden.ors_to_do.modules.dependencies.ui.layout;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Scratchpad extends VBox {
    private AppStats appStats;
    private TextArea brainDumpArea;
    private WebView markdownPreview;
    private WebEngine webEngine;

    public Scratchpad(AppStats appStats) {
        super(10);
        this.appStats = appStats;

        HBox.setHgrow(this, Priority.SOMETIMES);
        setPrefWidth(350);
        setMaxWidth(400);

        HBox scratchHeader = new HBox();
        scratchHeader.setAlignment(Pos.CENTER_LEFT);

        Label scratchpadLabel = new Label("Scratchpad");
        scratchpadLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleButton previewToggle = new ToggleButton("👁 Preview");
        previewToggle.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        scratchHeader.getChildren().addAll(scratchpadLabel, spacer, previewToggle);

        StackPane editorStack = new StackPane();
        VBox.setVgrow(editorStack, Priority.ALWAYS);

        brainDumpArea = new TextArea(appStats.getBrainDumpText());
        brainDumpArea.setStyle("-fx-control-inner-background: #1E1E1E; -fx-background-color: #1E1E1E; -fx-text-fill: #E0E0E0; -fx-font-family: 'Consolas', monospace; -fx-font-size: 15px; -fx-border-color: #3E3E42;");
        brainDumpArea.setWrapText(true);
        brainDumpArea.textProperty().addListener((obs, oldText, newText) -> appStats.setBrainDumpText(newText));

        markdownPreview = new WebView();
        webEngine = markdownPreview.getEngine();
        markdownPreview.setVisible(false);

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/atom-one-dark.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
                <style>
                    body { background-color: #1E1E1E; color: #E0E0E0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 15px; padding: 15px; margin: 0; line-height: 1.6; }
                    pre { background-color: #2D2D30; padding: 15px; border-radius: 8px; overflow-x: auto; border: 1px solid #3E3E42; }
                    code { font-family: 'Consolas', monospace; background-color: #2D2D30; padding: 3px 6px; border-radius: 4px; color: #4EC9B0; }
                    pre code { padding: 0; background-color: transparent; color: inherit; }
                    a { color: #569CD6; text-decoration: none; }
                    a:hover { text-decoration: underline; }
                    h1, h2, h3, h4 { color: #CCCCCC; border-bottom: 1px solid #3E3E42; padding-bottom: 5px; margin-top: 10px; }
                    blockquote { border-left: 4px solid #569CD6; margin: 0; padding-left: 15px; color: #AAAAAA; font-style: italic; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 15px; }
                    th, td { border: 1px solid #3E3E42; padding: 8px; text-align: left; }
                    th { background-color: #2D2D30; }
                    ::-webkit-scrollbar { width: 12px; height: 12px; }
                    ::-webkit-scrollbar-track { background: #1E1E1E; }
                    ::-webkit-scrollbar-thumb { background: #3E3E42; border-radius: 6px; border: 3px solid #1E1E1E; }
                    ::-webkit-scrollbar-thumb:hover { background: #555555; }
                </style>
            </head>
            <body>
                <div id="content"><span style="color:#AAAAAA; font-style:italic;">Initializing Markdown Engine...</span></div>
                <script>
                    function updateContent(base64Text) {
                        try {
                            const decodedText = decodeURIComponent(escape(atob(base64Text)));
                            document.getElementById('content').innerHTML = marked.parse(decodedText);
                            hljs.highlightAll();
                        } catch (e) {
                            document.getElementById('content').innerHTML = "<p style='color:#FF6666;'>Error parsing Markdown</p>";
                        }
                    }
                </script>
            </body>
            </html>
            """;

        webEngine.loadContent(htmlTemplate);

        previewToggle.setOnAction(e -> {
            if (previewToggle.isSelected()) {
                previewToggle.setText("✏️ Edit");
                brainDumpArea.setVisible(false);
                markdownPreview.setVisible(true);
                updateMarkdownPreview();
            } else {
                previewToggle.setText("👁 Preview");
                markdownPreview.setVisible(false);
                brainDumpArea.setVisible(true);
            }
        });

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && previewToggle.isSelected()) {
                updateMarkdownPreview();
            }
        });

        editorStack.getChildren().addAll(brainDumpArea, markdownPreview);

        Button clearDumpBtn = new Button("Clear Scratchpad");
        clearDumpBtn.setMaxWidth(Double.MAX_VALUE);
        clearDumpBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        clearDumpBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Clear your notes?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> { if (response == ButtonType.YES) brainDumpArea.clear(); updateMarkdownPreview(); });
        });

        getChildren().addAll(scratchHeader, editorStack, clearDumpBtn);
    }

    private void updateMarkdownPreview() {
        String text = brainDumpArea.getText();
        if (text == null) text = "";
        String base64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        webEngine.executeScript("updateContent('" + base64 + "');");
    }
}