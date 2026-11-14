package application.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import application.models.CommunityRecipe;
import application.models.Recipe;
import application.services.RecipeService;
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

    @FXML
    private void initialize() {
        renderRecipes();
    }

    @FXML
    private void filterRecipes(ActionEvent event) {
        renderRecipes();
    }

    private void renderRecipes() {
        List<CommunityRecipe> visibleRecipes = recipeService.getCommunityRecipes().stream()
                .sorted(Comparator.comparingInt(CommunityRecipe::popularity).reversed())
                .filter(this::matchesTime)
                .filter(this::matchesAllergies)
                .filter(this::matchesBudget)
                .filter(this::matchesMealType)
                .toList();

        popularRecipeCards.getChildren().clear();
        visibleRecipes.stream()
                .limit(3)
                .map(this::createRecipeCard)
                .forEach(popularRecipeCards.getChildren()::add);

        communityRecipeCards.getChildren().clear();
        visibleRecipes.stream()
                .map(this::createRecipeCard)
                .forEach(communityRecipeCards.getChildren()::add);

        if (visibleRecipes.isEmpty()) {
            Label emptyState = new Label("No community recipes match these filters.");
            emptyState.getStyleClass().add("empty-state");
            communityRecipeCards.getChildren().add(emptyState);
        }
    }

    private boolean matchesTime(CommunityRecipe listing) {
        int selectedLimit = selectedCheckBoxes(timeFilters).stream()
                .mapToInt(this::numericLimit)
                .min()
                .orElse(Integer.MAX_VALUE);
        return listing.recipe().totalTimeMinutes() < selectedLimit;
    }

    private boolean matchesAllergies(CommunityRecipe listing) {
        Set<String> exclusions = selectedCheckBoxes(allergyFilters).stream()
                .map(checkBox -> checkBox.getText().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> allergens = listing.allergens().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return exclusions.stream().noneMatch(allergen ->
                allergen.equals("nuts")
                        ? allergens.stream().anyMatch(value ->
                                value.equals("nuts") || value.equals("peanuts") || value.equals("tree nuts"))
                        : allergens.contains(allergen));
    }

    private boolean matchesBudget(CommunityRecipe listing) {
        int selectedLimit = selectedCheckBoxes(budgetFilters).stream()
                .mapToInt(this::numericLimit)
                .min()
                .orElse(Integer.MAX_VALUE);
        return listing.estimatedCostDollars() < selectedLimit;
    }

    private boolean matchesMealType(CommunityRecipe listing) {
        Set<String> selectedMeals = selectedCheckBoxes(mealFilters).stream()
                .map(checkBox -> checkBox.getText().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return selectedMeals.isEmpty()
                || selectedMeals.contains(listing.recipe().category().toLowerCase(Locale.ROOT));
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

    private VBox createRecipeCard(CommunityRecipe listing) {
        Recipe recipe = listing.recipe();
        return RecipeCardFactory.create(
                recipe,
                List.of(
                        "By " + listing.author() + " | $" + listing.estimatedCostDollars(),
                        (listing.popularity() >= 100 ? "Popular | " : "")
                                + listing.popularity() + " saves"),
                source -> openRecipe(source, recipe));
    }

    private void openRecipe(Node source, Recipe recipe) {
        goToViewRecipe(new ActionEvent(source, source), recipe);
    }
}
