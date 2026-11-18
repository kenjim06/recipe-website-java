package application.controllers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseException;
import application.services.UserService;
import application.ui.RecipeCardFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class ExploreRecipesController extends HeaderController {
    @FXML
    private VBox timeFilters;

    @FXML
    private VBox allergyFilters;

    @FXML
    private VBox budgetFilters;

    @FXML
    private VBox mealFilters;

    @FXML
    private FlowPane popularRecipeCards;

    @FXML
    private FlowPane communityRecipeCards;

    private final RecipeService recipeService = new RecipeService();
    private List<Recipe> recipes = List.of();
    private String loadError;
    private final Set<Long> favoriteRecipeIds = new HashSet<>();

    @FXML
    private void initialize() {
        try {
            recipes = recipeService.getHomePageRecipes();
        } catch (SupabaseException e) {
            loadError = e.getMessage();
        }
        if (UserService.isLoggedIn()) {
            try {
                favoriteRecipeIds.addAll(RecipeService.getFavoriteRecipeIds());
            } catch (SupabaseException ignored) {
                // Browse community recipes even if favorite state cannot be loaded.
            }
        }
        renderRecipes();
    }

    @FXML
    private void filterRecipes(ActionEvent event) {
        renderRecipes();
    }

    private void renderRecipes() {
        popularRecipeCards.getChildren().clear();
        communityRecipeCards.getChildren().clear();
        if (loadError != null) {
            showEmptyState(popularRecipeCards, loadError);
            showEmptyState(communityRecipeCards, loadError);
            return;
        }

        List<Recipe> visibleRecipes = recipes.stream()
                .sorted(Comparator.comparingInt(Recipe::popularity).reversed())
                .filter(this::matchesTime)
                .filter(this::matchesAllergies)
                .filter(this::matchesBudget)
                .filter(this::matchesMealType)
                .toList();

        visibleRecipes.stream()
                .limit(3)
                .map(this::createRecipeCard)
                .forEach(popularRecipeCards.getChildren()::add);

        visibleRecipes.stream()
                .map(this::createRecipeCard)
                .forEach(communityRecipeCards.getChildren()::add);

        if (visibleRecipes.isEmpty()) {
            showEmptyState(communityRecipeCards, "No community recipes match these filters.");
        }
    }

    private void showEmptyState(FlowPane target, String message) {
        Label emptyState = new Label(message);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setWrapText(true);
        target.getChildren().add(emptyState);
    }

    private boolean matchesTime(Recipe listing) {
        int selectedLimit = selectedCheckBoxes(timeFilters).stream()
                .mapToInt(this::numericLimit)
                .min()
                .orElse(Integer.MAX_VALUE);
        return listing.totalTimeMinutes() < selectedLimit;
    }

    private boolean matchesAllergies(Recipe listing) {
        Set<String> exclusions = selectedCheckBoxes(allergyFilters).stream()
                .map(checkBox -> checkBox.getText().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> allergens = Arrays.stream(
                listing.allergens().toLowerCase(Locale.ROOT).split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());

        return exclusions.stream().noneMatch(allergen ->
                allergen.equals("nuts")
                        ? allergens.stream().anyMatch(value ->
                                value.equals("nuts") || value.equals("peanuts") || value.equals("tree nuts"))
                        : allergens.contains(allergen));
    }

    private boolean matchesBudget(Recipe listing) {
        int selectedLimit = selectedCheckBoxes(budgetFilters).stream()
                .mapToInt(this::numericLimit)
                .min()
                .orElse(Integer.MAX_VALUE);
        return listing.estimatedCostDollars() < selectedLimit;
    }

    private boolean matchesMealType(Recipe listing) {
        Set<String> selectedMeals = selectedCheckBoxes(mealFilters).stream()
                .map(checkBox -> checkBox.getText().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return selectedMeals.isEmpty()
                || selectedMeals.contains(listing.category().toLowerCase(Locale.ROOT));
    }

    private List<CheckBox> selectedCheckBoxes(VBox filterGroup) {
        return filterGroup.getChildren().stream()
                .filter(CheckBox.class::isInstance)
                .map(CheckBox.class::cast)
                .filter(CheckBox::isSelected)
                .toList();
    }

    private int numericLimit(CheckBox checkBox) {
        String digits = checkBox.getText().replaceAll("[^0-9]", "");
        return Integer.parseInt(digits);
    }

    private VBox createRecipeCard(Recipe listing) {
        return RecipeCardFactory.create(
                listing,
                List.of(
                        "Ready in " + listing.totalTimeMinutes() + " min"),
                source -> openRecipe(source, listing), favoriteRecipeIds, null);
    }

    private void openRecipe(Node source, Recipe recipe) {
        goToViewRecipe(new ActionEvent(source, source), recipe);
    }
}
