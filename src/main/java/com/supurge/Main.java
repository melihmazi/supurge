package com.supurge;

import com.supurge.view.AnaEkran;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Uygulamanın başlangıç noktası.
 * BZ 214 Visual Programming - Robot Süpürge Simülasyonu
 *
 * This project was developed as part of the BZ 214 Visual Programming course.
 * Special thanks to the course instructor and contributors.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new AnaEkran().baslat(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
