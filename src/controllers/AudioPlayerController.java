package controllers;

import java.io.File;
import java.io.IOException;
import javaclasses.CurrentRecording;
import javaclasses.CustomAlert;
import javaclasses.RedirectFXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.beans.Observable;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AudioPlayerController {

    @FXML
    private Slider timeSlider;

    @FXML
    private Button playButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button backButton;

    @FXML
    private Button forwardButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label directoryLabel;

    @FXML
    private Button nextButton;

    private Media media;
    private MediaPlayer player;

    RedirectFXML redirectFXML = new RedirectFXML();

    public void initialize() {
        directoryLabel.setText(CurrentRecording.currentRecording);
        File file = new File("C:/Users/USER/Documents/NIIT/AudioDiary/src/recordings/" + CurrentRecording.currentRecording);
        media = new Media(file.toURI().toString());
        player = new MediaPlayer(media);
        playButton.setOnAction((ActionEvent e) -> {
            if (player.getCurrentTime().greaterThanOrEqualTo(player.getTotalDuration())) {
                player.seek(player.getStartTime());
            }
            player.play();
        });
        pauseButton.setOnAction((ActionEvent e) -> {
            player.pause();
        });
        stopButton.setOnAction((ActionEvent e) -> {
            player.stop();
        });
        backButton.setOnAction((ActionEvent e) -> {
            player.seek(player.getCurrentTime().multiply(0.2));
        });
        forwardButton.setOnAction((ActionEvent e) -> {
            player.seek(player.getCurrentTime().multiply(1.2));
        });
        player.currentTimeProperty().addListener((Observable ov) -> {
            updateTimeSlider();
        });

        timeSlider.valueProperty().addListener((Observable ov) -> {
            if (timeSlider.isPressed()) {
                player.seek(player.getMedia().getDuration().multiply(timeSlider.getValue() / 100));
            }
        });

        volumeSlider.valueProperty().addListener((Observable ov) -> {
            if (volumeSlider.isPressed()) {
                player.setVolume(volumeSlider.getValue() / 100);
            }
        });
        playButton.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        Stage primaryStage = (Stage) playButton.getScene().getWindow();
                        primaryStage.setOnCloseRequest((WindowEvent t) -> {
                            player.pause();
                        });
                        primaryStage.setOnHiding((WindowEvent t) -> {
                            player.pause();
                        });
                    }
                });
            }
        });
        playButton.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        Stage primaryStage = (Stage) playButton.getScene().getWindow();
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

    void updateTimeSlider() {
        Platform.runLater(() -> {
            timeSlider.setValue(player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis() * 100);
        });
    }

    @FXML
    void goToNextScene(ActionEvent event) throws IOException {
        player.stop();
        redirectFXML.redirect((Stage) nextButton.getScene().getWindow(), "/views/RecordingDetails.fxml",
                "Add Recording Details");
    }

}
