package javaclasses;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Recorder 
{
    File wavFile;
    private Boolean paused = false;
    private ArrayList<AudioInputStream> fileParts = new ArrayList<>();
    
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    TargetDataLine line;

    AudioFormat getAudioFormat() {
        /*
         * SAMPLE RATE: The number of samples played or recorded per-second
         * SAMPLE SIZE IN BITS: The number of bits in each sample of a sound 
         * CHANNELS: The number of audio channels in this format (1 for mono, 2 for stereo) 
         * SIGNED: Indicates whether the data is signed or unsigned
         * BIG ENDIAN: Indicates whether the data for a single sample is stored in big-endian byte order (false means little-endian) 
        */
        float sampleRate = 41000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    public void start() 
    {
        try {
            wavFile = File.createTempFile("recorder-", ".wav");
        } catch (IOException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing

            AudioInputStream ais = new AudioInputStream(line);

            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException | IOException ex) {
            System.out.println("An error occured in while starting record: "+ex.getMessage());
        }
    }
    
    public void pause()
    {
        if (!paused) {
            paused = true;
            line.stop();
            line.close();

            try {
                fileParts.add(AudioSystem.getAudioInputStream(wavFile));
            } catch (IOException ex) {
                System.err.println("An error occured " + ex.getMessage());
            } catch (UnsupportedAudioFileException ex) {
                System.err.println("An error occured: " + ex.getMessage());
            }
        } else {
            paused = false;
        }
    }

    public void finish() 
    {
        line.stop();
        line.close();
        try {
            if (!paused) {
                fileParts.add(AudioSystem.getAudioInputStream(wavFile));
            }
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
            String recordedTime = dateFormat.format(date)+".wav";
            CurrentRecording.currentRecording = recordedTime;
            File recordedFile = new File("src/recordings/"+recordedTime);

            Vector<InputStream> streams = new Vector<>();

            for (int i = 0; i < fileParts.size(); i++) {    
                streams.add(fileParts.get(i));
            }

            SequenceInputStream stream = new SequenceInputStream(streams.elements());
            int totalFrameLength = 0;
            for (AudioInputStream file : fileParts) {
                totalFrameLength += file.getFrameLength();
            }

            AudioInputStream appendedFiles = new AudioInputStream(stream, fileParts.get(0).getFormat(), totalFrameLength);
            AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, recordedFile);
            stream.close();
            streams.clear();
        } catch (IOException ex) {
            System.err.println("An error occured " + ex.getMessage());
        } catch (UnsupportedAudioFileException ex) {
            System.err.println("An error occured: " + ex.getMessage());
        }
    }
}
