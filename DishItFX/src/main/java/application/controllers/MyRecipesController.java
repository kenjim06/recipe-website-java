package application.controllers;

import javafx.fxml.FXML;

public class MyRecipesController extends RecipeCollectionController {
    @FXML
    private void initialize() {
        initializeCollection("MY RECIPES", true);
    }
}
