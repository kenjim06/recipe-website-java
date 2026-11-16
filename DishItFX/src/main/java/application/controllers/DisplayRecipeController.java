package application.controllers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import application.models.Recipe;
import application.ui.RecipeImageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class DisplayRecipeController extends HeaderController {
    @FXML
    private Label recipeNameLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label prepTimeLabel;

    @FXML
    private Label cookTimeLabel;

    @FXML
    private Label ingredientsText;

    @FXML
    private Label instructionsText;

    @FXML
    private Label notesText;

    @FXML
    private StackPane recipeImageFrame;

    @FXML
    private ImageView recipeImageView;

    public void setRecipe(Recipe recipe) {
        recipeNameLabel.setText(recipe.name());
        recipeNameLabel.setWrapText(true);
        recipeNameLabel.setMaxWidth(Double.MAX_VALUE);
        categoryLabel.setText(recipe.category());
        prepTimeLabel.setText(recipe.prepTimeMinutes() + " min");
        cookTimeLabel.setText(recipe.cookTimeMinutes() + " min");
        recipeImageFrame.prefHeightProperty().bind(
                recipeImageFrame.widthProperty().multiply(9.0 / 16.0));
        RecipeImageLoader.loadCover(recipeImageView, recipeImageFrame, recipe.imageUrl());
        ingredientsText.setText(formatIngredients(recipe.ingredients()));
        instructionsText.setText(formatInstructions(recipe.instructions()));
        notesText.setText(recipe.notes().isBlank() ? "No notes." : recipe.notes());
    }

    private static String formatIngredients(List<String> ingredients) {
        return ingredients.isEmpty()
                ? "No ingredients available yet."
                : ingredients.stream()
                        .map(ingredient -> "- " + ingredient)
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String formatInstructions(List<String> instructions) {
        return instructions.isEmpty()
                ? "No instructions available yet."
                : IntStream.range(0, instructions.size())
                        .mapToObj(index -> "Step " + (index + 1) + ":"
                                + System.lineSeparator() + instructions.get(index))
                        .collect(Collectors.joining(
                                System.lineSeparator() + System.lineSeparator()));
    }
}
