package controllers;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PAUSE;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PLAY;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import javaclasses.CurrentRecording;
import javaclasses.CustomAlert;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javaclasses.Recorder;
import javaclasses.RedirectFXML;

public class SoundRecorderController {

    @FXML
    private Circle outterCircle;

    @FXML
    private Button recordButton;

    @FXML
    private Circle innerCircle;

    @FXML
    private Label timeLabel;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;
    
    @FXML
    private FontAwesomeIconView pauseGlyph;

    private int hour = 0;
    private int minute = 0;
    private int second = 0;
    private Boolean altered = false;
    private Boolean paused = false;
    private Boolean stopped = false;
    private Boolean started = false;
    Recorder record = new Recorder();
    RedirectFXML redirectFXML = new RedirectFXML();

    public void initialize() {
        stopButton.setDisable(true);
        pauseButton.setDisable(true);
        stopButton.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        Stage primaryStage = (Stage) stopButton.getScene().getWindow();
                        primaryStage.setOnCloseRequest((WindowEvent event) -> {
                            if (started) {
                                event.consume();
                                CustomAlert.displayText = "Are you sure you want to exit?";
                                CustomAlert.yesBtn.setOnAction((ActionEvent arg0) -> {
                                    stopped = true;
                                    try {
                                        Thread.sleep(500); // wait for recording threads to finish
                                    } catch (InterruptedException ex) {
                                        System.err.println(ex.getMessage());
                                    }
                                    CurrentRecording.deleteFile();
                                    CustomAlert.dialogStage.close();
                                    primaryStage.close();
                                });
                                CustomAlert.noBtn.setOnAction((ActionEvent arg0) -> {
                                    CustomAlert.dialogStage.close();
                                });
                                CustomAlert.run();
                            }
                        });
                    }
                });
            }
        });
    }

    @FXML
    void pauseRecord(ActionEvent event) {
        if (!paused) {
            paused = true;
            record.pause();
            pauseGlyph.setIcon(PLAY);
        } else {
            paused = false;
            record.pause();
            pauseGlyph.setIcon(PAUSE);
            record();
            alternateColors();
            startTimer();
        }
    }

    @FXML
    void startRecord(ActionEvent event) {
        recordButton.setDisable(true);
        stopButton.setDisable(false);
        pauseButton.setDisable(false);
        record();
        alternateColors();
        startTimer();
        started = true;
    }

    @FXML
    void stopRecord(ActionEvent event) throws IOException {
        if (paused) {
            record.finish();
        } else {
            stopped = true;
        }
        try {
            Thread.sleep(1000); // give other processes a second to complete
        } catch (InterruptedException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
        redirectFXML.redirect((Stage) stopButton.getScene().getWindow(), "/views/AudioPlayer.fxml", "Preview Recording");
    }

    void startTimer() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!paused && !stopped) {
                    updateLabel();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        System.err.println("An error occured: " + ex.getMessage());
                    }
                }
            }
        };
        thread.start();
    }

    void updateLabel() {
        Platform.runLater(() -> {
            ++second;
            if (second == 60) {
                second = 0;
                ++minute;
            }
            if (minute == 60) {
                minute = 0;
                ++hour;
            }
            hour = hour % 24;
            
            timeLabel.setText(String.format("%02d:%02d:%02d", hour, minute, second));
        });
    }

    void alternateColors() {
        /*
         * this method alternates the colors in the sound records every 0.6 seconds
        */
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!paused && !stopped) {
                    innerCircle.setStyle("-fx-fill:steelblue");
                    if (altered) {
                        outterCircle.setStyle("-fx-fill:lightblue");
                    } else {
                        outterCircle.setStyle("-fx-fill:white");
                    }
                    altered = !altered;
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException ex) {
                        System.err.println("An error occured: " + ex.getMessage());
                    }
                }
            }
        };
        thread.start();
    }

    void record() {
        /*
         * this method starts a new recorder thread and stops when paused or stopped
        */
        Thread thread = new Thread() {
            @Override
            public void run() {
                Thread stopper = new Thread() {
                    @Override
                    public void run() {
                        while (!paused && !stopped) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                System.err.println("An error occured " + ex.getMessage());
                            }
                        }
                        if (stopped) {
                            record.finish();
                        }
                    }
                };
                stopper.start();
                record.start();
            }
        };
        thread.start();
    }
}
