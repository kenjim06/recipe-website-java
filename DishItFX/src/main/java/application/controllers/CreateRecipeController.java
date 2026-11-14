package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CreateRecipeController extends NavigationController {
	@FXML
	private TextField recipeNameField;
	private TextField prepTimeField;
	private TextField cookTimeField;
	private TextField ingredientsField;
	private TextField instructionsField;
	private TextField notesField;
	@FXML
	private void testName() { //A test to see if the text passed in the field goes through
		String checker = recipeNameField.getText();
		System.out.println("Recipe name: " + checker);
		
	}
}
