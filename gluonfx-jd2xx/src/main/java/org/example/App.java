package org.example;

import io.github.ozkanpakdil.JD2XX;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Label labelWithJD2XXInfo = new Label();

            JD2XX chosenPort = new JD2XX();
            chosenPort.open(0);
            chosenPort.setBaudRate(250000);
            chosenPort.setDataCharacteristics(JD2XX.BITS_8, JD2XX.STOP_BITS_2, JD2XX.PARITY_NONE);
            chosenPort.setFlowControl(JD2XX.FLOW_NONE, 0, 0);
            labelWithJD2XXInfo.setText(String.valueOf(chosenPort.getDeviceInfo()));

            var scene = new Scene(new StackPane(labelWithJD2XXInfo), 800, 500);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to initialize JD2XX: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}