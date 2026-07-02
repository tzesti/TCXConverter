package com.github.tzesti.tcxconverter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TcxConverterApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(TcxConverterApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 960, 720);
        stage.setTitle("TCX Converter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
