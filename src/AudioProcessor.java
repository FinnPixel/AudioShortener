import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioProcessor {

    public static void shortenAudio(File fileInput, int startSecond, int endSecond, File fileOutput)
            throws UnsupportedAudioFileException, IOException {
        try (AudioInputStream encoded = AudioSystem.getAudioInputStream(fileInput);
             AudioInputStream decoded = toPcm(encoded)) {

            AudioFormat format = decoded.getFormat();
            float frameRate = format.getFrameRate();
            int frameSize = format.getFrameSize();

            skipExactly(decoded, (long) (startSecond * frameRate) * frameSize);
            long framesToCopy = (long) ((endSecond - startSecond) * frameRate);

            try (AudioInputStream shortened = new AudioInputStream(decoded, format, framesToCopy)) {
                AudioSystem.write(shortened, AudioFileFormat.Type.WAVE, fileOutput);
            }
        }
    }

    public static long getDurationInSeconds(File fileInput) {
        try {
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(fileInput);

            Object microseconds = fileFormat.properties().get("duration");
            if (microseconds instanceof Long) {
                return Math.round((Long) microseconds / 1_000_000.0);
            }

            long frameLength = fileFormat.getFrameLength();
            float frameRate = fileFormat.getFormat().getFrameRate();
            if (frameLength != AudioSystem.NOT_SPECIFIED && frameRate != AudioSystem.NOT_SPECIFIED) {
                return Math.round(frameLength / (double) frameRate);
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static AudioInputStream toPcm(AudioInputStream stream) {
        AudioFormat source = stream.getFormat();
        if (source.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
                || source.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
            return stream;
        }
        AudioFormat target = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                source.getSampleRate(),
                16,
                source.getChannels(),
                source.getChannels() * 2,
                source.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(target, stream);
    }

    private static void skipExactly(AudioInputStream stream, long bytes) throws IOException {
        long remaining = bytes;
        while (remaining > 0) {
            long skipped = stream.skip(remaining);
            if (skipped <= 0) {
                if (stream.read() == -1) {
                    return;
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }
}
