package application.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;


import application.services.UserService;

public class LoginController extends NavigationController {
	@FXML
	private TextField emailTF;
	@FXML
	private PasswordField passwordTF;
	
	@FXML
	private void login(ActionEvent event) {
		String email = emailTF.getText();
		String password = passwordTF.getText();
		
		String token = UserService.userLogin(email, password);
		
		if(token != null && !token.isBlank()) {
			goHome(event);
		} else {
			System.out.println("Invalid email/password");
		}
		
	}
}
