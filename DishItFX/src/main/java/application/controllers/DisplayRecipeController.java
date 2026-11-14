package application.controllers;

import java.util.List;

import application.models.Recipe;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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
    private ListView<String> ingredientsList;

    @FXML
    private ListView<String> instructionsList;

    @FXML
    private ListView<String> notesList;

    public void setRecipe(Recipe recipe) {
        recipeNameLabel.setText(recipe.name());
        categoryLabel.setText(recipe.category());
        prepTimeLabel.setText(recipe.prepTimeMinutes() + " min");
        cookTimeLabel.setText(recipe.cookTimeMinutes() + " min");
        setListContent(ingredientsList, recipe.ingredients(), "No ingredients available yet.");
        setListContent(instructionsList, recipe.instructions(), "No instructions available yet.");
        setListContent(notesList,
                recipe.notes().isBlank() ? List.of() : List.of(recipe.notes()),
                "No notes.");
    }

    private void setListContent(ListView<String> listView, List<String> values, String emptyMessage) {
        listView.setItems(FXCollections.observableArrayList(values));
        listView.setPlaceholder(new Label(emptyMessage));
    }
}
