package javaclasses;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RedirectFXML 
{
    public void redirect(Stage currentStage, String directory, String title)
    {
        try 
        {
            Parent root = FXMLLoader.load(getClass().getResource(directory));
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.setResizable(false);
            currentStage.centerOnScreen();
            currentStage.show();       
        }
        catch(IOException ex)
        {
            System.err.println("An error occured "+ex.getMessage());
        }
    }
    
    public void redirect(String directory, String title)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getResource(directory));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        }
        catch(IOException ex)
        {
            System.err.println("An error occured "+ex.getMessage());
        }
    }
    
    public void redirectCloseCurrent(Stage currentStage, String directory, String title)
    {
        try
        {
            currentStage.close();
            Parent root = FXMLLoader.load(getClass().getResource(directory));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        }
        catch(IOException ex)
        {
            System.err.println("An error occured: "+ex.getMessage());
        }
    }
    public void close(Stage currentStage)
    {
        currentStage.close();
    }
}
