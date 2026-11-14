package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends HeaderController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginFeedback;

    @FXML
    private void loginToAccount() {
        if (emailField.getText().isBlank() || passwordField.getText().isBlank()) {
            loginFeedback.setText("Enter your email and password.");
            return;
        }
        loginFeedback.setText("Sign in is unavailable until the account API is connected.");
    }
}
