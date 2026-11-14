
package application;

import application.ui.PageFrame;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            loadBundledFont("/fonts/Nunito-Variable.ttf");
            loadBundledFont("/fonts/Raleway-Variable.ttf");
            loadBundledFont("/fonts/Comic_CAT.otf");
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/HomePage.fxml")
            );


            Parent root = PageFrame.wrap(loader.load());


            Scene scene = new Scene(root, 1100, 720);
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

    private void loadBundledFont(String resource) {
        try (InputStream stream = Main.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Bundled font resource not found: " + resource);
            }
            if (Font.loadFont(stream, 12.0) == null) {
                throw new IllegalStateException("Could not load bundled font: " + resource);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read bundled font: " + resource, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
