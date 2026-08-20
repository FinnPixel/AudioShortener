import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class GUI extends JFrame {

    private static final int SLIDER_RESOLUTION = 1000;
    private static final long MAX_DURATION_SECONDS = 360000;

    private JLabel selectedFileTitle;
    private JLabel startLabel;
    private JLabel endLabel;
    private RangeSlider rangeSlider;
    private JButton exportFile;

    private File fileInput;
    private long durationInSeconds;
    private JFileChooser fileChooser;

    public void init() {
        setTitle("AudioShortener");
        setSize(900, 600);
        setLocationRelativeTo(null);

        selectedFileTitle = new JLabel("no audio opened");
        selectedFileTitle.setFont(new Font("Verdana", Font.BOLD, 20));
        selectedFileTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        rangeSlider = new RangeSlider(0, SLIDER_RESOLUTION);
        rangeSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        rangeSlider.setEnabled(false);
        rangeSlider.addChangeListener(change -> updateTimeLabels());

        startLabel = new JLabel("00:00:00");
        endLabel = new JLabel("00:00:00");
        JPanel times = new JPanel(new BorderLayout());
        times.add(startLabel, BorderLayout.WEST);
        times.add(endLabel, BorderLayout.EAST);

        JPanel range = new JPanel();
        range.setLayout(new BoxLayout(range, BoxLayout.Y_AXIS));
        range.setMaximumSize(new Dimension(600, 80));
        range.setAlignmentX(Component.CENTER_ALIGNMENT);
        range.add(rangeSlider);
        range.add(times);

        JButton openFile = new JButton("open audio file");
        openFile.addActionListener(change -> openFileEvent());

        exportFile = new JButton("export file");
        exportFile.setEnabled(false);
        exportFile.addActionListener(change -> exportFileEvent());

        JPanel buttons = new JPanel();
        buttons.add(openFile);
        buttons.add(exportFile);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelInner = new JPanel();
        panelInner.setLayout(new BoxLayout(panelInner, BoxLayout.Y_AXIS));
        panelInner.add(Box.createVerticalGlue());
        panelInner.add(selectedFileTitle);
        panelInner.add(Box.createRigidArea(new Dimension(0, 40)));
        panelInner.add(range);
        panelInner.add(Box.createRigidArea(new Dimension(0, 40)));
        panelInner.add(buttons);
        panelInner.add(Box.createVerticalGlue());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(panelInner, BorderLayout.CENTER);
        mainPanel.add(Box.createRigidArea(new Dimension(60, 0)), BorderLayout.EAST);
        mainPanel.add(Box.createRigidArea(new Dimension(60, 0)), BorderLayout.WEST);
        add(mainPanel);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        warmUpFileChooser();
    }

    
    private void warmUpFileChooser() {
        new Thread(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("audio files (*.wav, *.mp3)", "wav", "mp3"));
            chooser.setAcceptAllFileFilterUsed(false);
            SwingUtilities.invokeLater(() -> fileChooser = chooser);
        }, "file-chooser-warmup").start();
    }

    private JFileChooser fileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("audio files (*.wav, *.mp3)", "wav", "mp3"));
            fileChooser.setAcceptAllFileFilterUsed(false);
        }
        return fileChooser;
    }

    private void openFileEvent() {
        JFileChooser chooser = fileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        long duration = AudioProcessor.getDurationInSeconds(selected);
        if (duration <= 0) {
            showError("Unable to read this audio file.");
            return;
        }
        if (duration >= MAX_DURATION_SECONDS) {
            showError("This audio file is too long.");
            return;
        }

        fileInput = selected;
        durationInSeconds = duration;
        selectedFileTitle.setText(selected.getName());
        rangeSlider.setEnabled(true);
        rangeSlider.setValues(0, SLIDER_RESOLUTION);
        exportFile.setEnabled(true);
        updateTimeLabels();
    }

    private void exportFileEvent() {
        int start = sliderValueToSeconds(rangeSlider.getLowValue());
        int end = sliderValueToSeconds(rangeSlider.getHighValue());
        if (end <= start) {
            showError("The selected range is empty.");
            return;
        }

        JFileChooser chooser = fileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileOutput = new File(chooser.getSelectedFile().getAbsolutePath().replaceAll("(?i)\\.wav$", "") + ".wav");
        try {
            AudioProcessor.shortenAudio(fileInput, start, end, fileOutput);
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private void updateTimeLabels() {
        startLabel.setText(formatSeconds(sliderValueToSeconds(rangeSlider.getLowValue())));
        endLabel.setText(formatSeconds(sliderValueToSeconds(rangeSlider.getHighValue())));
    }

    private int sliderValueToSeconds(int sliderValue) {
        return (int) (durationInSeconds * sliderValue / (double) SLIDER_RESOLUTION);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "AudioShortener", JOptionPane.ERROR_MESSAGE);
    }

    private static String formatSeconds(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
}
