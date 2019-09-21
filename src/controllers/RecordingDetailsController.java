package controllers;

import db.DatabaseResources;
import java.time.LocalDate;
import javaclasses.CurrentRecording;
import javaclasses.CurrentUser;
import javaclasses.CustomAlert;
import javaclasses.RedirectFXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.Recording;

public class RecordingDetailsController {

    @FXML
    private TextField titleTextField;

    @FXML
    private ComboBox<String> moodComboBox;

    @FXML
    private TextArea commentTextField;

    @FXML
    private Button addButton;
    
    @FXML
    private Label titleInvalidation;

    DatabaseResources db = new DatabaseResources();
    RedirectFXML redirectFXML = new RedirectFXML();

    public void initialize() {
        ObservableList<String> data = FXCollections.observableArrayList("Happy", "Sad", "Love", "Excited", "Angry", "Fear", "Other");
        moodComboBox.setItems(data);
        moodComboBox.setValue("Happy");

        addButton.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        Stage primaryStage = (Stage) addButton.getScene().getWindow();
                        primaryStage.setOnCloseRequest((WindowEvent event) -> {
                            event.consume();
                            CustomAlert.displayText = "Are you sure you want to discard recording?";
                            CustomAlert.yesBtn.setOnAction((ActionEvent arg0) -> {
                                CurrentRecording.deleteFile();
                                CustomAlert.dialogStage.close();
                                primaryStage.close();
                            });
                            CustomAlert.noBtn.setOnAction((ActionEvent arg0) -> {
                                CustomAlert.dialogStage.close();
                            });
                            CustomAlert.run();
                        });
                    }
                });
            }
        });
    }

    @FXML
    void addRecord(ActionEvent event) {
        String title = titleTextField.getText();
        if(title.length()==0)
        {
            titleInvalidation.setText("Invalid recording title");
            return ;
        }
        
        String comments = commentTextField.getText();
        String mood = moodComboBox.getValue();
        Recording record = new Recording(CurrentUser.currentUser.getUserName(), title, mood, comments,
                LocalDate.now().getMonthValue(), LocalDate.now().getDayOfWeek().getValue(),
                LocalDate.now().getDayOfMonth(), LocalDate.now().getYear(), CurrentRecording.currentRecording);
        record.addRecording();
        redirectFXML.close((Stage) addButton.getScene().getWindow());
       
        DatabaseResources.changed = true; 
    }

}
