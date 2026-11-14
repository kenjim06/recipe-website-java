package application.controllers;

import application.services.SupabaseClient.SupabaseException;
import application.services.UserService;
import javafx.event.ActionEvent;
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
    private void submitLogin(ActionEvent event) {
        String email = emailField.getText().strip();
        String password = passwordField.getText();
        if (email.isBlank() || password.isBlank()) {
            loginFeedback.setText("Enter your email and password.");
            return;
        }

        try {
            UserService.userLogin(email, password);
            goHome(event);
        } catch (SupabaseException e) {
            loginFeedback.setText(e.getMessage());
        } catch (IllegalArgumentException e) {
            loginFeedback.setText(e.getMessage());
        }
    }
}
