package javaclasses;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomAlert {
    public static String displayText = "";
    public static Stage dialogStage = new Stage();
    public static Button noBtn = new Button("No");
    public static Button yesBtn = new Button("Yes");
    private static Boolean initialized = false;

    public static void run() 
    {
        if (!initialized) 
        {
            initialized = true;
            dialogStage.initModality(Modality.WINDOW_MODAL);
        }
        Label exitLabel = new Label(displayText);
        exitLabel.setAlignment(Pos.BASELINE_CENTER);
        
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_CENTER);
        hBox.setSpacing(40.0);
        hBox.getChildren().addAll(yesBtn, noBtn);
        
        VBox vBox = new VBox();
        vBox.setSpacing(40.0);
        vBox.getChildren().addAll(exitLabel, hBox);
        
        dialogStage.setScene(new Scene(vBox));
        dialogStage.setResizable(false);
        dialogStage.centerOnScreen();
        dialogStage.showAndWait();
    }
}
