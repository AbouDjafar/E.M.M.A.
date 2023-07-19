package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Acceuil.fxml"));
        primaryStage.setTitle("E.M.M.A. v0.1");
        Scene sc = new Scene(root, 1000, 600);
        sc.setFill(Color.TRANSPARENT);
        primaryStage.setScene(sc);
        primaryStage.getIcons().add(new Image(getClass().getResource("IMG/v4/InscrireOFF.png").toString()));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
