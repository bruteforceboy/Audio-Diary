package controllers;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PAUSE;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PLAY;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javaclasses.RedirectFXML;
import models.Recording;
import javaclasses.CurrentUser;
import javaclasses.CustomAlert;
import db.DatabaseResources;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import javafx.stage.WindowEvent;

public class AudioDiaryMainController {

    @FXML
    private ComboBox<String> dayComboBox;

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private ComboBox<String> yearComboBox;

    @FXML
    private Button homeButton;

    @FXML
    private Label currentUserLabel;

    @FXML
    private TableView<Recording> tableView;

    @FXML
    private TableColumn<Recording, String> titleColumn;

    @FXML
    private TableColumn<?, ?> dateColumn;

    @FXML
    private TableColumn<?, ?> moodColumn;

    @FXML
    private TextField titleSearchField;

    @FXML
    private TextField moodSearchField;

    @FXML
    private TextField commentsSearchField;

    @FXML
    private Slider timeSlider;

    @FXML
    private Slider volumeSlider;

    @FXML
    private FontAwesomeIconView pauseGlyph;

    private MediaPlayer player;
    private Boolean paused = false;
    private Boolean started = false;
    private Boolean initBefore = false;
    private Boolean appClosed = false;
    DatabaseResources db = new DatabaseResources();
    RedirectFXML redirectFXML = new RedirectFXML();
    ObservableList<Recording> currentUserRecords;

    public void initialize() {
        displayRecords();
        if (!initBefore) {
            setComboBox();
            initBefore = true;
        }
        addListeners();
        dbChangeListener();
    }

    public void displayRecords() {
        currentUserLabel.setText(CurrentUser.currentUser.getUserName());
        /*
         * get all recordings belonging to current user
         */
        Recording record = new Recording();
        currentUserRecords = record.getRecords(CurrentUser.currentUser.getUserName());
        ObservableList<Recording> data = FXCollections.observableArrayList(currentUserRecords);
        /*
         * set tableView to hold all recordings belonging to current user
         */
        tableView.setItems(data);
    }

    public void setComboBox() {
        ArrayList<String> days = new ArrayList<>();
        ArrayList<String> years = new ArrayList<>();
        ArrayList<String> months = new ArrayList<>();

        days.add("(none)");
        years.add("(none)");
        months.add("(none)");

        // add all days to ArrayList days
        for (int day = 1; day <= 31; day++) {
            days.add(day + "");
        }

        // add all years to ArrayList years
        for (int year = 2000; year <= LocalDate.now().getYear(); year++) {
            years.add(year + "");
        }

        // add all months to ArrayList months
        months.addAll(Arrays.asList(new DateFormatSymbols().getMonths()));
        months.remove(months.size() - 1);

        //set all comboBox items + default values
        dayComboBox.setItems(FXCollections.observableArrayList(days));
        dayComboBox.setValue("(none)");
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.setValue("(none)");
        monthComboBox.setItems(FXCollections.observableArrayList(months));
        monthComboBox.setValue("(none)");
    }

    public void addListeners() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("convertedDate"));
        moodColumn.setCellValueFactory(new PropertyValueFactory<>("mood"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        /*
         * creating new context menu to enable editing of title 
         * setOnAction also closes editable when done
         */
        ContextMenu cm = new ContextMenu();
        MenuItem mi1 = new MenuItem("edit title");
        cm.getItems().add(mi1);
        mi1.setOnAction((ActionEvent event) -> {
            tableView.setEditable(true);
            titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            titleColumn.setEditable(true);
            titleColumn.setOnEditCommit((CellEditEvent<Recording, String> t) -> {
                t.getRowValue().updateRecordingTitle(t.getNewValue());
                ObservableList<Recording> filteredList = tableView.getItems();
                for (Recording record : filteredList) {
                    if (record == t.getRowValue()) {
                        record.setTitle(t.getNewValue());
                    }
                }
                for (Recording record : currentUserRecords) {
                    if (record == t.getRowValue()) {
                        record.setTitle(t.getNewValue());
                    }
                }
                tableView.setEditable(false);
                titleColumn.setEditable(false);
            });
            titleColumn.setOnEditCancel((CellEditEvent<Recording, String> t) -> {
                tableView.setEditable(false);
                titleColumn.setEditable(false);
            });

            // start edit on selected row 
            tableView.getSelectionModel().select(tableView.getSelectionModel().selectedIndexProperty().get());
            tableView.layout();
            tableView.edit(tableView.getSelectionModel().selectedIndexProperty().get(), titleColumn);

        });

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if (t.getButton() == MouseButton.SECONDARY) {
                cm.show(tableView, t.getScreenX(), t.getScreenY()); // show context menu on right click
            }
        });

        /*
         * all search fields call method filterRecordings() on change of value
         */
        titleSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRecordings();
        });
        moodSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRecordings();
        });
        commentsSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRecordings();
        });

        // calls start player if row was double clicked
        tableView.setRowFactory(tv -> {
            TableRow<Recording> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Recording rowData = row.getItem();
                    startPlayer(rowData.getURL());
                }
            });
            return row;
        });

        // stop method dbChangeListener when window is closed
        homeButton.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        Stage primaryStage = (Stage) homeButton.getScene().getWindow();
                        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            @Override
                            public void handle(WindowEvent event) {
                                appClosed = true;
                            }
                        });
                    }
                });
            }
        });
    }

    public void dbChangeListener() {
        /*
         * this method listens for change in database 
         * and calls method initialize when change found
         * thread also stops when window is closed 
         */
        Thread refreshThread = new Thread() {
            @Override
            public void run() {
                while (!appClosed) {
                    if (DatabaseResources.changed) {
                        DatabaseResources.changed = false;
                        initialize();
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        System.err.println("An error occured: " + ex.getMessage());
                    }
                }
            }
        };
        refreshThread.start();
    }

    void startPlayer(String url) {
        if (started) {
            player.stop(); // stops previous playing before starting a new one 
        }
        if (paused) {
            // if previous was still on pause, unpause and start new one
            paused = false;
            pauseGlyph.setIcon(PAUSE);
        }

        File file = new File("C:/Users/USER/Documents/NIIT/AudioDiary/src/recordings/" + url);
        Media media = new Media(file.toURI().toString());
        player = new MediaPlayer(media);
        player.play();

        started = true;

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
    }

    void updateTimeSlider() {
        // add timeSlider to UI thread queue 
        Platform.runLater(() -> {
            timeSlider.setValue(player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis() * 100);
        });
    }

    @FXML
    void pausePlayer(ActionEvent event) {
        if(!started)
            return ;
        /*
             * if player not previously paused, pause it and change glyph
             * else if player was previously paused, unpause it and restore initial glpyh
         */
        if (!paused) {
            player.pause();
            paused = true;
            pauseGlyph.setIcon(PLAY);
        } else {
            player.play();
            paused = false;
            pauseGlyph.setIcon(PAUSE);
        }
    }

    @FXML
    void stopPlayer(ActionEvent event) {
        if(!started)
            return ;
        
        player.stop();
        paused = false;
        pauseGlyph.setIcon(PAUSE); // set icon back to default
    }

    public void filterRecordings() {
        /*
         * this method creates a new list 
         * that satisfies all the charactersitics specified in the text fields
         */
        ObservableList<Recording> newList = FXCollections.observableArrayList();

        String moodToFilter = moodSearchField.getText();
        String titleToFilter = titleSearchField.getText();
        String commentsToFilter = commentsSearchField.getText();

        // converts all values to lower case for uniformity
        moodToFilter = moodToFilter.toLowerCase();
        titleToFilter = titleToFilter.toLowerCase();
        commentsToFilter = commentsToFilter.toLowerCase();

        // remove all ending spaces in strings from all text fields 
        while (moodToFilter.endsWith(" ")) {
            moodToFilter = moodToFilter.substring(0, moodToFilter.length() - 1);
        }
        while (titleToFilter.endsWith(" ")) {
            titleToFilter = titleToFilter.substring(0, titleToFilter.length() - 1);
        }
        while (commentsToFilter.endsWith(" ")) {
            commentsToFilter = commentsToFilter.substring(0, commentsToFilter.length() - 1);
        }

        for (Recording record : currentUserRecords) {
            // get all values for current record and also convert to lowercase 
            String mood = record.getMood().toLowerCase();
            String title = record.getTitle().toLowerCase();
            String comments = record.getComments().toLowerCase();

            Boolean valid = true;

            // check if substring of string matches with string from each text fields if not empty
            if (moodToFilter.length() > 0 && mood.contains(moodToFilter) == false) {
                valid = false;
            }
            if (titleToFilter.length() > 0 && title.contains(titleToFilter) == false) {
                valid = false;
            }
            if (commentsToFilter.length() > 0 && comments.contains(commentsToFilter) == false) {
                valid = false;
            }

            // add to new list only if valid
            if (valid) {
                newList.add(record);
            }
        }

        // set new list
        tableView.setItems(newList);

        // NOTE: in this function the current user records list isn't altered
    }

    public void deleteFile(String url) {
        File file = new File("C:/Users/USER/Documents/NIIT/AudioDiary/src/recordings/" + url);

        Boolean successful = file.delete();

        if (!successful) {
            file.deleteOnExit(); // delete later
        }
    }

    @FXML
    void deleteSelected(ActionEvent event) {
        if(tableView.getSelectionModel().selectedIndexProperty().get() == -1)
            return ; // returns nothing if no item in table view was selected
        
        if (paused) {
            paused = false;
            pauseGlyph.setIcon(PAUSE); // set icon back to default  
        }
        if (started) {
            player.stop();
        }
        CustomAlert.displayText = "Are you sure you want to delete?";
        CustomAlert.yesBtn.setOnAction((ActionEvent arg) -> {
            ObservableList<Recording> filteredList = tableView.getItems();
            ObservableList<Recording> records = tableView.getSelectionModel().getSelectedItems();
            for (Recording record : records) {
                deleteFile(record.getURL()); // delete file
                record.deleteRecord(); // delete record
                filteredList.remove(record);
                currentUserRecords.remove(record);
            }
            CustomAlert.dialogStage.close();
        });

        CustomAlert.noBtn.setOnAction((ActionEvent arg0) -> {
            CustomAlert.dialogStage.close();
        });

        CustomAlert.run();
    }

    @FXML
    void addNewRecording(ActionEvent event) throws IOException {
        if (started) {
            player.stop();
            paused = false;
            pauseGlyph.setIcon(PAUSE);
        }
        redirectFXML.redirect("/views/SoundRecorder.fxml", "Audio Recorder");
    }

    @FXML
    void filterResults(ActionEvent event) {
        /*
         * this method filters results according to selected day, month and year
         */

        String month = monthComboBox.getValue();
        int day = ("(none)".equals(dayComboBox.getValue())) ? 0 : Integer.parseInt(dayComboBox.getValue());
        int year = ("(none)".equals(yearComboBox.getValue())) ? 0 : Integer.parseInt(yearComboBox.getValue());

        int monthvalue = 0;
        String months[] = new DateFormatSymbols().getMonths();
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                monthvalue = i;
            }
        }

        ArrayList<Recording> newList = new ArrayList<>();
        for (Recording record : currentUserRecords) {
            if (((record.getDay() == day) || day == 0) && ((record.getMonth() == monthvalue) || month.equals("(none)")) && ((record.getYear() == year) || year == 0)) {
                newList.add(record);
            }
        }
        // set new values
        ObservableList<Recording> data = FXCollections.observableArrayList(newList);
        tableView.setItems(data);
    }

    @FXML
    void goBackHome(ActionEvent event) {
        if (started) {
            player.stop();
        }
        appClosed = true;
        redirectFXML.redirectCloseCurrent((Stage) homeButton.getScene().getWindow(), "/views/Login.fxml", "Audio Diary Login");
    }

    @FXML
    void resetAudioDiary(ActionEvent event) {
        initialize();
    }

    @FXML
    void viewHelp(ActionEvent event) {
        redirectFXML.redirect("/views/Help.fxml", "Audio Diary Help");
    }

}
