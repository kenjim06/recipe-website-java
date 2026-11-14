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

    private final RecipeService recipeService = new RecipeService();
    private List<Recipe> recipes;

    @FXML
    private void initialize() {
        recipes = recipeService.getHomePageRecipes().stream() // Pull from database here
                .limit(MAX_HOME_PAGE_RECIPES)
                .toList();

        ToggleGroup categoryGroup = new ToggleGroup();
        categoryFilters.getChildren().stream()
                .filter(ToggleButton.class::isInstance)
                .map(ToggleButton.class::cast)
                .forEach(button -> button.setToggleGroup(categoryGroup));
        allCategoryButton.setSelected(true);

        renderRecipes();
    }

    @FXML
    private void filterRecipes(ActionEvent event) {
        renderRecipes();
    }

    private void renderRecipes() {
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

        recipeCards.getChildren().clear();
        visibleRecipes.stream()
                .map(recipe -> RecipeCardFactory.create(
                        recipe,
                        List.of("Ready in " + recipe.totalTimeMinutes() + " min"),
                        source -> openRecipe(source, recipe)))
                .forEach(recipeCards.getChildren()::add);

        int count = visibleRecipes.size();
        resultsLabel.setText(count + (count == 1 ? " recipe" : " recipes"));
        if (count == 0) {
            Label emptyState = new Label("No recipes match your search and category.");
            emptyState.getStyleClass().add("empty-state");
            recipeCards.getChildren().add(emptyState);
        }
    }

    private void openRecipe(Node source, Recipe recipe) {
        goToViewRecipe(new ActionEvent(source, source), Objects.requireNonNull(recipe));
    }
}
