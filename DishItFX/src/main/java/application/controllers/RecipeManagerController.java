package application.controllers;

import javafx.fxml.FXML;

public class RecipeManagerController extends RecipeCollectionController {
    @FXML
    private void initialize() {
        initializeCollection("RECIPE MANAGER");
    }
}
