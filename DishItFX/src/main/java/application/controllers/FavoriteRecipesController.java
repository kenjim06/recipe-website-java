package application.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseException;
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
    private List<Recipe> recipes = List.of();
    private String loadError;
    private final Set<Long> favoriteRecipeIds = new HashSet<>();

    @FXML
    private void initialize() {
        try {
            recipes = recipeService.getSavedRecipes();
            recipes.stream().map(Recipe::id).forEach(favoriteRecipeIds::add);
        } catch (SupabaseException e) {
            loadError = e.getMessage();
        }
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

        recipeCards.getChildren().clear();
        if (loadError != null) {
            resultsLabel.setText("Could not load saved recipes.");
            showEmptyState(loadError);
            return;
        }

        String query = searchQuery.toLowerCase(Locale.ROOT);
        List<Recipe> visibleRecipes = recipes.stream()
                .filter(recipe -> query.isEmpty()
                        || recipe.name().toLowerCase(Locale.ROOT).contains(query)
                        || recipe.category().toLowerCase(Locale.ROOT).contains(query))
                .toList();

        visibleRecipes.stream()
                .map(recipe -> RecipeCardFactory.create(
                        recipe,
                        List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                        source -> openRecipe(source, recipe), favoriteRecipeIds, null,
                        isFavorite -> {
                            if (!isFavorite) {
                                recipes = recipes.stream()
                                        .filter(saved -> saved.id() != recipe.id())
                                        .toList();
                                renderRecipes();
                            }
                        }))
                .forEach(recipeCards.getChildren()::add);

        int count = visibleRecipes.size();
        resultsLabel.setText(count + (count == 1 ? " saved recipe" : " saved recipes"));
        if (count == 0) {
            showEmptyState(query.isEmpty()
                    ? "You have no saved recipes yet."
                    : "No saved recipes match your search.");
        }
    }

    private void showEmptyState(String message) {
        Label emptyState = new Label(message);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setWrapText(true);
        recipeCards.getChildren().add(emptyState);
    }

    private void openRecipe(Node source, Recipe recipe) {
        goToViewRecipe(new ActionEvent(source, source), Objects.requireNonNull(recipe));
    }
}
