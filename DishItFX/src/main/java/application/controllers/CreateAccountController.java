package application.controllers;

import application.models.User;
import application.services.SupabaseException;
import application.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class CreateAccountController extends HeaderController {
    @FXML
    private TextField displayNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label formFeedback;

    private final UserService userService = new UserService();

    @FXML
    private void saveAccount(ActionEvent event) {
        String displayName = displayNameField.getText().strip();
        String email = emailField.getText().strip();
        String password = passwordField.getText();

        if (displayName.isEmpty() || email.isEmpty() || password.isBlank()) {
            formFeedback.setText("Enter your name, email, and password.");
            return;
        }
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            formFeedback.setText("Enter a valid email address.");
            return;
        }
        if (!password.equals(confirmPasswordField.getText())) {
            formFeedback.setText("The passwords do not match.");
            return;
        }

        try {
            userService.saveAccount(new User(displayName, email), password);
            formFeedback.setText("Account created. You can now log in.");
        } catch (SupabaseException e) {
            formFeedback.setText(e.getMessage());
        }
    }

    @FXML
    private void returnToLogin(ActionEvent event) {
        goToLogin(event);
    }
}
