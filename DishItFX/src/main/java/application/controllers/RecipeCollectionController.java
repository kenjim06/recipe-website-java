package application.controllers;

import java.util.List;
import java.util.Objects;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseClient.SupabaseException;
import application.ui.RecipeCardFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public abstract class RecipeCollectionController extends HeaderController {
    @FXML
    private Label pageTitle;

    @FXML
    private Label resultsLabel;

    @FXML
    private FlowPane recipeCards;

    private final RecipeService recipeService = new RecipeService();

    protected final void initializeCollection(String title) {
        pageTitle.setText(title);
        renderRecipes();
    }

    private void renderRecipes() {
        recipeCards.getChildren().clear();
        List<Recipe> recipes;
        try {
            recipes = recipeService.getMyRecipes();
        } catch (SupabaseException e) {
            resultsLabel.setText("Could not load recipes.");
            Label errorState = new Label(e.getMessage());
            errorState.getStyleClass().add("empty-state");
            recipeCards.getChildren().add(errorState);
            return;
        }
        recipes.stream()
                .map(recipe -> RecipeCardFactory.create(
                        recipe,
                        List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                        source -> openRecipe(source, recipe)))
                .forEach(recipeCards.getChildren()::add);

        int count = recipes.size();
        resultsLabel.setText(count + (count == 1 ? " recipe" : " recipes"));
        if (count == 0) {
            Label emptyState = new Label("Your recipes will appear here after you create one.");
            emptyState.getStyleClass().add("empty-state");
            recipeCards.getChildren().add(emptyState);
        }
    }

    private void openRecipe(Node source, Recipe recipe) {
        goToViewRecipe(new ActionEvent(source, source), Objects.requireNonNull(recipe));
    }
}
