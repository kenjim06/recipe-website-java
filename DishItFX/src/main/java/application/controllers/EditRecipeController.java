package application.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class EditRecipeController extends HeaderController {
    @FXML
    private ChoiceBox<Recipe> recipeChoiceBox;

    @FXML
    private TextField recipeNameField;

    @FXML
    private ChoiceBox<String> categoryChoiceBox;

    @FXML
    private TextField prepTimeField;

    @FXML
    private TextField cookTimeField;

    @FXML
    private TextArea ingredientsField;

    @FXML
    private TextArea instructionsField;

    @FXML
    private TextArea notesField;

    @FXML
    private Label formFeedback;

    @FXML
    private void initialize() {
        categoryChoiceBox.setItems(FXCollections.observableArrayList(
                "Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Snack"));
        recipeChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Recipe recipe) {
                return recipe == null ? "" : recipe.name();
            }

            @Override
            public Recipe fromString(String value) {
                return null;
            }
        });
        recipeChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) -> populateFields(selected));

        try {
            List<Recipe> recipes = RecipeService.get();
            recipeChoiceBox.setItems(FXCollections.observableArrayList(recipes));
            if (recipes.isEmpty()) {
                formFeedback.setText("You do not have any recipes to edit.");
                return;
            }
            recipeChoiceBox.getSelectionModel().selectFirst();
        } catch (SupabaseException e) {
            formFeedback.setText(e.getMessage());
        }
    }

    @FXML
    private void saveChanges(ActionEvent event) {
        Recipe selected = recipeChoiceBox.getValue();
        if (selected == null) {
            formFeedback.setText("Choose a recipe to edit.");
            return;
        }

        String name = recipeNameField.getText().strip();
        String category = categoryChoiceBox.getValue();
        List<String> ingredients = lines(ingredientsField.getText());
        List<String> instructions = lines(instructionsField.getText());
        if (name.isEmpty() || category == null || ingredients.isEmpty() || instructions.isEmpty()) {
            formFeedback.setText("Add a recipe name, category, ingredients, and instructions.");
            return;
        }

        try {
            int prepTime = Integer.parseInt(prepTimeField.getText().strip());
            int cookTime = Integer.parseInt(cookTimeField.getText().strip());
            RecipeService.update(
                    selected.id(),
                    name,
                    List.of(category),
                    prepTime,
                    cookTime,
                    String.join(System.lineSeparator(), ingredients),
                    String.join(System.lineSeparator(), instructions),
                    notesField.getText());
            goToMyRecipes(event);
        } catch (NumberFormatException e) {
            formFeedback.setText("Prep and cook times must be whole numbers of minutes.");
        } catch (SupabaseException | IllegalArgumentException e) {
            formFeedback.setText(e.getMessage());
        }
    }

    private void populateFields(Recipe recipe) {
        if (recipe == null) {
            return;
        }
        recipeNameField.setText(recipe.name());
        categoryChoiceBox.setValue(recipe.category());
        prepTimeField.setText(Integer.toString(recipe.prepTimeMinutes()));
        cookTimeField.setText(Integer.toString(recipe.cookTimeMinutes()));
        ingredientsField.setText(String.join(System.lineSeparator(), recipe.ingredients()));
        instructionsField.setText(String.join(System.lineSeparator(), recipe.instructions()));
        notesField.setText(recipe.notes());
        formFeedback.setText("");
    }

    private List<String> lines(String value) {
        return Arrays.stream(value.split("\\R"))
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }
}
