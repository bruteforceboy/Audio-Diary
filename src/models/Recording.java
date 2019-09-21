package models;

import db.DatabaseResources;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Recording {

    private int id;
    private String username;
    private String title;
    private String mood;
    private String comments;
    private String convertedDate;
    private int month;
    private int date;
    private int dayOfWeek; // day of week
    private int year;
    private String URL;

    public Recording() {
    }

    public Recording(String username, String title, String mood, String comments, int month, int dayOfWeek, int date, int year, String URL) {
        this.username = username;
        this.title = title;
        this.mood = mood;
        this.comments = comments;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.date = date;
        this.year = year;
        this.URL = URL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return date;
    }

    public void setDay(int day) {
        this.date = day;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getConvertedDate() {
        return convertedDate;
    }

    public void setConvertedDate() {
        String months[] = new DateFormatSymbols().getMonths();
        String days[] = new DateFormatSymbols().getWeekdays();
        String dayString = days[dayOfWeek];
        String monthString = months[month];
        String dateString = date + "";

        switch (date) {
            case 1:
            case 21:
            case 31:
                dateString += "st";
                break;
            case 2:
            case 22:
                dateString += "nd";
                break;
            case 3:
            case 23:
                dateString += "rd";
                break;
            default:
                dateString += "th";
                break;
        }

        convertedDate = dayString + " " + (dateString) + " " + monthString + ", " + (year + "") + ".";
    }

    public void addRecording() {
        int recordingDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int dateOfRecording = Calendar.getInstance().get(Calendar.DATE);
        int monthOfRecording = Calendar.getInstance().get(Calendar.MONTH);
        int yearOfRecording = Calendar.getInstance().get(Calendar.YEAR);

        final String QUERY = "INSERT INTO dbo.[RECORDINGS] ([Username],[Title],[Mood],[Comments],[Month]"
                + ",[Day],[Date],[Year],[URL]) VALUES (?,?,?,?,?,?,?,?,?)";
        String[] returnID = {"ID"};
        try (Connection conn = DatabaseResources.getConnection();
                PreparedStatement statement = conn.prepareStatement(QUERY, returnID)) {
            statement.setString(1, username);
            statement.setString(2, title);
            statement.setString(3, mood);
            statement.setString(4, comments);
            statement.setInt(5, monthOfRecording);
            statement.setInt(6, recordingDayOfWeek);
            statement.setInt(7, dateOfRecording);
            statement.setInt(8, yearOfRecording);
            statement.setString(9, URL);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Now rows were inserted");
            } else {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
    }

    public ObservableList<Recording> getRecords(String username) {
        ObservableList<Recording> records = FXCollections.observableArrayList();
        final String QUERY = "SELECT * FROM dbo.[RECORDINGS] WHERE [USERNAME] = ?";
        try (Connection conn = DatabaseResources.getConnection();
                PreparedStatement statement = conn.prepareStatement(QUERY)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Recording record = new Recording(rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getString(10));
                records.add(record);
                record.setConvertedDate();
                record.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
        return records;
    }

    public void deleteRecord() {
        final String QUERY = "DELETE FROM dbo.[RECORDINGS] WHERE [ID]=?";
        try (Connection conn = DatabaseResources.getConnection();
                PreparedStatement statement = conn.prepareStatement(QUERY)) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("An error occured no rows were deleted from dbo.[RECORDINGS]");
            }
        } catch (SQLException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
    }

    public void updateRecordingTitle(String newTitle) {
        final String QUERY = "UPDATE dbo.[RECORDINGS] SET title=? WHERE [ID]=?";
        try (Connection conn = DatabaseResources.getConnection();
                PreparedStatement statement = conn.prepareStatement(QUERY)) {
            statement.setString(1, newTitle);
            statement.setInt(2, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("No rows were affected");
            }
        } catch (SQLException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
    }
}
