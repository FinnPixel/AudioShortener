import java.io.*;
import javax.sound.sampled.*;

public class AudioProcessor {

    public static void shortenAudio(File fileInput, int skipSeconds, int cutAtEnd, File fileOutput) {
        AudioInputStream inputStream = null;
        AudioInputStream shortenedStream = null;
        try {
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(fileInput);
            AudioFormat format = fileFormat.getFormat();
            inputStream = AudioSystem.getAudioInputStream(fileInput);
            int bytesPerSecond = format.getFrameSize() * (int)format.getFrameRate();
            inputStream.skip((long) skipSeconds * bytesPerSecond);
            long framesOfAudioToCopy = (long) (cutAtEnd - skipSeconds) * (int)format.getFrameRate();
            shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
            AudioSystem.write(shortenedStream, fileFormat.getType(), fileOutput);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) try { inputStream.close(); } catch (Exception e) { System.out.println(e); }
            if (shortenedStream != null) try { shortenedStream.close(); } catch (Exception e) { System.out.println(e); }
        }
    }

    public static long getDurationInSeconds(File fileInput) {
        AudioInputStream inputStream;
        AudioFileFormat fileFormat;
        double secs;
        try {
            fileFormat = AudioSystem.getAudioFileFormat(fileInput);
            inputStream = AudioSystem.getAudioInputStream(fileInput);
            AudioFormat format = fileFormat.getFormat();
            secs = (inputStream.getFrameLength() + 0.0) / format.getFrameRate();
            return Math.round(secs);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
