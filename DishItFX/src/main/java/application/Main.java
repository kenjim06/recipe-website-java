
package application;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/Login.fxml")
            );


            Parent root = loader.load();


            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(
                getClass().getResource("application.css").toExternalForm()
            );

            primaryStage.setTitle("Dish It");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
