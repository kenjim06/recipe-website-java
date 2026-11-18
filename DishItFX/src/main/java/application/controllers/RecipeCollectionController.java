package application.controllers;

import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseException;
import application.services.UserService;
import application.ui.RecipeCardFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;

public abstract class RecipeCollectionController extends HeaderController {
    @FXML
    private Label pageTitle;

    @FXML
    private Label resultsLabel;

    @FXML
    private FlowPane recipeCards;

    private final RecipeService recipeService = new RecipeService();
    private boolean managerView;
    private final Set<Long> favoriteRecipeIds = new HashSet<>();

    protected final void initializeCollection(String title) {
        initializeCollection(title, false);
    }

    protected final void initializeCollection(String title, boolean managerView) {
        this.managerView = managerView;
        pageTitle.setText(title);
        renderRecipes();
    }

    private void renderRecipes() {
        recipeCards.getChildren().clear();
        if (UserService.isGuest()) {
            resultsLabel.setText("Guest mode");
            Label signInState = new Label("Log in or create an account to manage your recipes.");
            signInState.getStyleClass().add("empty-state");
            signInState.setWrapText(true);
            recipeCards.getChildren().add(signInState);
            return;
        }
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
        favoriteRecipeIds.clear();
        try {
            favoriteRecipeIds.addAll(RecipeService.getFavoriteRecipeIds());
        } catch (SupabaseException ignored) {
            // Keep showing owned recipes even if favorite state cannot be loaded.
        }
        recipes.stream()
                .map(recipe -> managerView
                        ? RecipeCardFactory.create(recipe,
                                List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                                source -> openRecipe(source, recipe), favoriteRecipeIds,
                                source -> deleteRecipe(recipe))
                        : RecipeCardFactory.create(recipe,
                                List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                                source -> openRecipe(source, recipe), favoriteRecipeIds, null))
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

    private void deleteRecipe(Recipe recipe) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + recipe.name() + "\"? This cannot be undone.",
                ButtonType.CANCEL, ButtonType.YES);
        confirmation.setHeaderText("Delete recipe");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) {
            return;
        }
        try {
            RecipeService.delete(recipe.id());
            renderRecipes();
        } catch (SupabaseException e) {
            Alert error = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            error.setHeaderText("Could not delete recipe");
            error.showAndWait();
        }
    }
}
