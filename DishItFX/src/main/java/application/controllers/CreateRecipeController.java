package application.controllers;

import java.util.Arrays;
import java.util.List;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseException;
import application.services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateRecipeController extends HeaderController {
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

    private final RecipeService recipeService = new RecipeService();

    @FXML
    private void initialize() {
        categoryChoiceBox.setItems(FXCollections.observableArrayList(
                "Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Snack"));
        categoryChoiceBox.setValue("Dinner");
    }

    @FXML
    private void saveRecipe(ActionEvent event) {
        String recipeName = recipeNameField.getText().strip();
        String category = categoryChoiceBox.getValue();
        List<String> ingredients = lines(ingredientsField.getText());
        List<String> instructions = lines(instructionsField.getText());

        if (recipeName.isEmpty() || category == null || ingredients.isEmpty() || instructions.isEmpty()) {
            formFeedback.setText("Add a recipe name, category, ingredients, and instructions.");
            return;
        }
        if (!UserService.isLoggedIn()) {
            formFeedback.setText("Log in before saving a recipe to Supabase.");
            return;
        }

        try {
            int prepTime = Integer.parseInt(prepTimeField.getText().strip());
            int cookTime = Integer.parseInt(cookTimeField.getText().strip());
            Recipe recipe = new Recipe(
                    0L,
                    recipeName,
                    category,
                    prepTime,
                    cookTime,
                    ingredients,
                    instructions,
                    notesField.getText(),
                    "",
                    "",
                    0,
                    "",
                    0);
            recipeService.saveRecipe(recipe);

            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setHeaderText("Recipe saved");
            confirmation.setContentText("This recipe was saved to your Supabase account.");
            confirmation.showAndWait();
            goToMyRecipes(event);
        } catch (NumberFormatException e) {
            formFeedback.setText("Prep and cook times must be whole numbers of minutes.");
        } catch (SupabaseException e) {
            formFeedback.setText(e.getMessage());
        } catch (IllegalArgumentException e) {
            formFeedback.setText(e.getMessage());
        }
    }

    private List<String> lines(String value) {
        return Arrays.stream(value.split("\\R"))
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
    }
}
