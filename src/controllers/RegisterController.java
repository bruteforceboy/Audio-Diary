package controllers;

import db.DatabaseResources;
import javaclasses.CurrentUser;
import javaclasses.RedirectFXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.User;

public class RegisterController {

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField userNameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private Label firstNameInvalidation;

    @FXML
    private Label lastNameInvalidation;

    @FXML
    private Label userNameInvalidation;

    @FXML
    private Label passwordMismatch;

    @FXML
    private Button signUpButton;

    @FXML
    private Button backButton;
    
    @FXML
    private AnchorPane anchorPane;
    
    
    DatabaseResources db = new DatabaseResources();
    RedirectFXML redirectFXML = new RedirectFXML();
    
    public void initialize() {
        // make anchorpane default active component
        Platform.runLater(() -> {
            anchorPane.requestFocus();
        });
    }
    
    @FXML
    void backToLogin(ActionEvent event) 
    {
        redirectFXML.redirect((Stage)backButton.getScene().getWindow(),"/views/Login.fxml","Audio Diary Login");
    }
    
    @FXML
    void goToDiary(ActionEvent event) {
        Boolean valid = true;
        User user = new User();
        try
        {
            user.setFirstName(firstNameTextField.getText());
        }
        catch(IllegalArgumentException ex)
        {
            valid=false;
            firstNameInvalidation.setText(ex.getMessage());
        }
        
        try
        {
            user.setLastName(lastNameTextField.getText());
        }
        catch(IllegalArgumentException ex)
        {
            valid=false;
            lastNameInvalidation.setText(ex.getMessage());
        }
        
        if(!passwordTextField.getText().equals(confirmPasswordTextField.getText()))
        {
            valid=false;
            passwordMismatch.setText("Both Passwords don't match");
        }
        
        try
        {
            user.setPassword(passwordTextField.getText());
        }
        catch(IllegalArgumentException ex)
        {
            passwordMismatch.setText(ex.getMessage());
        }
        
        /*
         * username validation takes place if all others were valid
         * this helps to save computing time
        */
        if (valid) {
            try {
                user.setUserName(userNameTextField.getText());
            } catch (IllegalArgumentException ex) {
                valid = false;
                userNameInvalidation.setText(ex.getMessage());
            }
        }
        
        if (valid) {
            user.insertUser();
            CurrentUser.currentUser = user;
            redirectFXML.redirect((Stage) signUpButton.getScene().getWindow(), "/views/AudioDiaryMain.fxml", "Audio Diary");
        }
    }

}
