package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class HeaderController extends NavigationController {
    @FXML
    private TextField searchField;

    @FXML
    private MenuButton accountButton;

    @FXML
    private Button homeNavButton;

    @FXML
    private Button myRecipesNavButton;

    @FXML
    private Button favoritesNavButton;

    @FXML
    private Button discoverNavButton;

    @FXML
    private Button recipeManagerButton;

    @FXML
    private Label brandDishLabel;

    @FXML
    private Label brandItLabel;

    @FXML
    private void initialize() {
        setBrandFont();
        configureRecipeManagerHover();
        switch (getCurrentPage()) {
            case "HomePage" -> select(homeNavButton);
            case "MyRecipes" -> select(myRecipesNavButton);
            case "FavoriteRecipes" -> select(favoritesNavButton);
            case "ExploreRecipes" -> select(discoverNavButton);
            default -> {
            }
        }
    }

    @FXML
    private void searchSavedRecipes(ActionEvent event) {
        searchSavedRecipes(event, searchField.getText().strip());
    }

    @FXML
    private void continueAsGuest() {
        goHome(new ActionEvent(accountButton, accountButton));
    }

    @FXML
    private void createAccount() {
        goToCreateAccount(new ActionEvent(accountButton, accountButton));
    }

    @FXML
    private void loginToAccount() {
        goToLogin(new ActionEvent(accountButton, accountButton));
    }

    private void select(Button button) {
        if (button != null) {
            button.getStyleClass().add("selected");
        }
    }

    private void setBrandFont() {
        if (brandDishLabel == null || brandItLabel == null) {
            return;
        }

        if (!Font.getFamilies().contains("Comic CAT")) {
            throw new IllegalStateException("Comic CAT font was not loaded from the application resources.");
        }
        Font brandFont = Font.font("Comic CAT", FontWeight.BOLD, 30.0);
        brandDishLabel.setFont(brandFont);
        brandItLabel.setFont(brandFont);
    }

    private void configureRecipeManagerHover() {
        if (recipeManagerButton == null) {
            return;
        }

        ScaleTransition grow = new ScaleTransition(Duration.millis(140), recipeManagerButton);
        grow.setToX(1.05);
        grow.setToY(1.05);
        ScaleTransition reset = new ScaleTransition(Duration.millis(140), recipeManagerButton);
        reset.setToX(1.0);
        reset.setToY(1.0);

        recipeManagerButton.setOnMouseEntered(event -> {
            reset.stop();
            grow.playFromStart();
        });
        recipeManagerButton.setOnMouseExited(event -> {
            grow.stop();
            reset.playFromStart();
        });
    }
}
