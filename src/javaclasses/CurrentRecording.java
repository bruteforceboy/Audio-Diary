package javaclasses;

import java.io.File;

public class CurrentRecording 
{
    public static String currentRecording = "";
    
    public static void deleteFile()
    {
        File file = new File("C:/Users/USER/Documents/NIIT/AudioDiary/src/recordings/" + currentRecording);
        
        file.deleteOnExit(); // this deletes file on exit because other processes might be using current file
    }
}
