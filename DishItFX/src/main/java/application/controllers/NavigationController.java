package application.controllers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;

import application.models.Recipe;
import application.ui.PageFrame;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public abstract class NavigationController {
    private static String currentPage = "HomePage";

    protected static String getCurrentPage() {
        return currentPage;
    }

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

    @FXML
    public final void goToCreateAccount(ActionEvent event) {
        navigate(event, "CreateAccount");
    }

    @FXML
    public final void goToExploreRecipes(ActionEvent event) {
        navigate(event, "ExploreRecipes");
    }

    protected final void searchSavedRecipes(ActionEvent event, String query) {
        String searchQuery = Objects.requireNonNull(query, "query").strip();
        navigate(event, "FavoriteRecipes", loader ->
                loader.<FavoriteRecipesController>getController().setSearchQuery(searchQuery));
    }

    @FXML
    public final void goToDisplayRecipe(ActionEvent event) {
        navigate(event, "DisplayRecipe");
    }

    public final void goToViewRecipe(ActionEvent event, Recipe recipe) {
        Recipe selectedRecipe = Objects.requireNonNull(recipe, "recipe");
        navigate(event, "DisplayRecipe", loader ->
                loader.<DisplayRecipeController>getController().setRecipe(selectedRecipe));
    }

    private void navigate(ActionEvent event, String page) {
        navigate(event, page, loader -> { });
    }

    private void navigate(ActionEvent event, String page, Consumer<FXMLLoader> configurePage) {
        String resource = "/application/" + page + ".fxml";
        String previousPage = currentPage;
        currentPage = page;

        try {
            FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
                    getClass().getResource(resource),
                    "Page resource not found: " + resource
                )
            );
            Parent root = loader.load();
            configurePage.accept(loader);
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(PageFrame.wrap(root));
        } catch (IOException e) {
            currentPage = previousPage;
            throw new UncheckedIOException("Could not load page: " + resource, e);
        } catch (RuntimeException e) {
            currentPage = previousPage;
            throw e;
        }
    }
}
