package application.controllers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public abstract class NavigationController {
    @FXML
    public final void goHome(ActionEvent event) {
        navigate(event, "HomePage");
    }

    @FXML
    public final void goToMyRecipes(ActionEvent event) {
        navigate(event, "MyRecipes");
    }

    @FXML
    public final void goToFavoriteRecipes(ActionEvent event) {
        navigate(event, "FavoriteRecipes");
    }

    @FXML
    public final void goToRecipeManager(ActionEvent event) {
        navigate(event, "RecipeManager");
    }

    @FXML
    public final void goToEditRecipe(ActionEvent event) {
        navigate(event, "EditRecipe");
    }

    @FXML
    public final void goToCreateRecipe(ActionEvent event) {
        navigate(event, "CreateRecipe");
    }

    @FXML
    public final void goToLogin(ActionEvent event) {
        navigate(event, "Login");
    }

    private void navigate(ActionEvent event, String page) {
        String resource = "/application/" + page + ".fxml";
        FXMLLoader loader = new FXMLLoader(
            Objects.requireNonNull(
                getClass().getResource(resource),
                "Page resource not found: " + resource
            )
        );

        try {
            Parent root = loader.load();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load page: " + resource, e);
        }
    }
}
