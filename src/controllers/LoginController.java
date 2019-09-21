package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import db.DatabaseResources;
import java.io.IOException;
import javaclasses.CurrentUser;
import javaclasses.RedirectFXML;
import models.User;

public class LoginController{

    @FXML
    private AnchorPane anchorPane;
    
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Label invalidationLabel;
    
    @FXML
    private Button logInButton;

    @FXML
    private Hyperlink signUpLink;
    
    DatabaseResources db = new DatabaseResources();
    RedirectFXML redirectFXML = new RedirectFXML();
    
    public void initialize() {
        /**
         * make anchor pane active by default
         * add to UI thread queue
         */
        Platform.runLater(() -> {
            anchorPane.requestFocus();
        });
    }
    
    @FXML
    void createNewAccount(ActionEvent event) 
    {
        redirectFXML.redirect((Stage)signUpLink.getScene().getWindow(), "/views/Register.fxml", "Create Account");
    }

    @FXML
    void loginToDiary(ActionEvent event) throws IOException
    {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        
        User currentUser = User.isValidUser(username, password);
        
        if(currentUser==null)
        {
            invalidationLabel.setText("Incorrect username/password");
        }
        else
        {
            CurrentUser.currentUser = currentUser; // set global variable user
            
            redirectFXML.redirect((Stage)logInButton.getScene().getWindow(), "/views/AudioDiaryMain.fxml",
            "Audio Diary");            
        }
    }
}
