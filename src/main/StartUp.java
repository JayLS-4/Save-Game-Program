package main;

import gui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartUp extends Application {

    private MainWindow mainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            mainWindow = new MainWindow(stage);
            Scene scene = new Scene(mainWindow);
            stage.setScene(scene);
            stage.setTitle("Save Game Program");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void stop() {
//        if (mainWindow != null) {
//            mainWindow.shutdownExecutorService();
//        }
//    }

    public static void main(String[] args) {
        launch();
    }
}
