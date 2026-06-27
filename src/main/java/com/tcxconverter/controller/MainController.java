package com.tcxconverter.controller;

import com.tcxconverter.formatter.CsvFormatter;
import com.tcxconverter.model.TcxData;
import com.tcxconverter.parser.TcxParser;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.List;

public class MainController {

    @FXML private TextArea outputTextArea;
    @FXML private Label statusLabel;
    @FXML private Button copyButton;

    private final TcxParser parser = new TcxParser();
    private final CsvFormatter formatter = new CsvFormatter();

    @FXML
    public void initialize() {
        copyButton.setDisable(true);
    }

    @FXML
    public void onDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (isTcxDrag(db)) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    public void onDragEntered(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            statusLabel.setText(isTcxDrag(db) ? "Release to convert..." : "Only .tcx files are supported");
        }
        event.consume();
    }

    @FXML
    public void onDragExited(DragEvent event) {
        statusLabel.setText(outputTextArea.getText().isEmpty()
                ? "Drop a .tcx file here to convert"
                : statusLabel.getText());
        event.consume();
    }

    @FXML
    public void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (isTcxDrag(db)) {
            File file = db.getFiles().get(0);
            try {
                TcxData data = parser.parse(file);
                String csv = formatter.format(data);
                outputTextArea.setText(csv);
                long trackpointCount = data.activities().stream()
                        .flatMap(a -> a.laps().stream())
                        .mapToLong(l -> l.trackpoints().size())
                        .sum();
                statusLabel.setText("Loaded: " + file.getName()
                        + " — " + trackpointCount + " trackpoints");
                copyButton.setDisable(false);
                success = true;
            } catch (Exception e) {
                outputTextArea.setText("Error parsing file:\n" + e.getMessage());
                statusLabel.setText("Failed to parse " + file.getName());
                copyButton.setDisable(true);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void onCopyClicked() {
        String text = outputTextArea.getText();
        if (!text.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
            statusLabel.setText("Copied to clipboard!");
        }
    }

    private boolean isTcxDrag(Dragboard db) {
        if (!db.hasFiles()) return false;
        List<File> files = db.getFiles();
        return files.size() == 1 && files.get(0).getName().toLowerCase().endsWith(".tcx");
    }
}
