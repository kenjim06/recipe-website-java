package application.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import application.models.Recipe;
import application.services.RecipeService;
import application.ui.RecipeCardFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class FavoriteRecipesController extends HeaderController {
    @FXML
    private FlowPane recipeCards;

    @FXML
    private Label resultsLabel;

    private final RecipeService recipeService = new RecipeService();
    private String searchQuery = "";

    @FXML
    private void initialize() {
        renderRecipes();
    }

    public void setSearchQuery(String query) {
        searchQuery = Objects.requireNonNull(query, "query").strip();
        renderRecipes();
    }

    private void renderRecipes() {
        if (recipeCards == null || resultsLabel == null) {
            return;
        }

        String query = searchQuery.toLowerCase(Locale.ROOT);
        List<Recipe> visibleRecipes = recipeService.getSavedRecipes().stream()
                .filter(recipe -> query.isEmpty()
                        || recipe.name().toLowerCase(Locale.ROOT).contains(query)
                        || recipe.category().toLowerCase(Locale.ROOT).contains(query))
                .toList();

        recipeCards.getChildren().clear();
        visibleRecipes.stream()
                .map(recipe -> RecipeCardFactory.create(
                        recipe,
                        List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                        source -> openRecipe(source, recipe)))
                .forEach(recipeCards.getChildren()::add);

        int count = visibleRecipes.size();
        resultsLabel.setText(count + (count == 1 ? " saved recipe" : " saved recipes"));
        if (count == 0) {
            Label emptyState = new Label(query.isEmpty()
                    ? "You have no saved recipes yet."
                    : "No saved recipes match your search.");
            emptyState.getStyleClass().add("empty-state");
            recipeCards.getChildren().add(emptyState);
        }
    }

    private void openRecipe(Node source, Recipe recipe) {
        goToViewRecipe(new ActionEvent(source, source), Objects.requireNonNull(recipe));
    }
}
