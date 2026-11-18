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
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class HomePageController extends HeaderController {
    private static final int MAX_HOME_PAGE_RECIPES = 20;

    @FXML
    private HBox categoryFilters;

    @FXML
    private ToggleButton allCategoryButton;

    @FXML
    private FlowPane recipeCards;

    @FXML
    private Label resultsLabel;

    @FXML
    private Label pageHeading;

    private final RecipeService recipeService = new RecipeService();
    private List<Recipe> recipes = List.of();
    private String loadError;
    private String searchQuery = "";
    private final Set<Long> favoriteRecipeIds = new HashSet<>();

    @FXML
    private void initialize() {
        ToggleGroup categoryGroup = new ToggleGroup();
        categoryFilters.getChildren().stream()
                .filter(ToggleButton.class::isInstance)
                .map(ToggleButton.class::cast)
                .forEach(button -> button.setToggleGroup(categoryGroup));
        allCategoryButton.setSelected(true);

        try {
            recipes = recipeService.getHomePageRecipes().stream()
                    .limit(MAX_HOME_PAGE_RECIPES)
                    .toList();
        } catch (SupabaseException e) {
            loadError = e.getMessage();
        }
        if (UserService.isLoggedIn()) {
            try {
                favoriteRecipeIds.addAll(RecipeService.getFavoriteRecipeIds());
            } catch (SupabaseException ignored) {
                // Recipe browsing remains available if favorite state cannot be loaded.
            }
        }

        renderRecipes();
    }

    @FXML
    private void filterRecipes(ActionEvent event) {
        renderRecipes();
    }

    public void setSearchQuery(String query) {
        searchQuery = Objects.requireNonNullElse(query, "").strip();
        loadError = null;
        try {
            if (searchQuery.isEmpty()) {
                recipes = recipeService.getHomePageRecipes().stream()
                        .limit(MAX_HOME_PAGE_RECIPES)
                        .toList();
            } else {
                recipes = recipeService.searchRecipes(searchQuery);
                allCategoryButton.setSelected(true);
            }
        } catch (SupabaseException e) {
            recipes = List.of();
            loadError = e.getMessage();
        }
        renderRecipes();
    }

    private void renderRecipes() {
        pageHeading.setText(searchQuery.isEmpty() ? "LATEST RECIPES" : "SEARCH RESULTS");
        recipeCards.getChildren().clear();
        if (loadError != null) {
            resultsLabel.setText("Could not load recipes.");
            showEmptyState(loadError);
            return;
        }

        Toggle selectedToggle = categoryFilters.getChildren().stream()
                .filter(ToggleButton.class::isInstance)
                .map(ToggleButton.class::cast)
                .filter(ToggleButton::isSelected)
                .findFirst()
                .orElseGet(() -> {
                    allCategoryButton.setSelected(true);
                    return allCategoryButton;
                });
        String selectedCategory = ((ToggleButton) selectedToggle).getText();

        List<Recipe> visibleRecipes = recipes.stream()
                .filter(recipe -> "All".equals(selectedCategory)
                        || recipe.category().equalsIgnoreCase(selectedCategory))
                .toList();

        visibleRecipes.stream()
                .map(recipe -> RecipeCardFactory.create(
                        recipe,
                        List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                        source -> openRecipe(source, recipe), favoriteRecipeIds, null))
                .forEach(recipeCards.getChildren()::add);

        int count = visibleRecipes.size();
        resultsLabel.setText(count + (count == 1 ? " recipe" : " recipes")
                + (searchQuery.isEmpty() ? "" : " found"));
        if (count == 0) {
            showEmptyState(searchQuery.isEmpty()
                    ? "No recipes match this category."
                    : "No recipes in the database match your search.");
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
