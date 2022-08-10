import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class GUI extends JFrame {

    private final JPanel mainPanel = new JPanel();
    private final JPanel panelInner = new JPanel();
    private final BorderLayout mainLayout = new BorderLayout();

    private JLabel selectedFile;
    private JSlider cutEnd;
    private JSlider cutFront;
    private File fileInput = null;

    JLabel currentCutOffEnd = new JLabel();
    JLabel currentCutOffFront = new JLabel();

    private final FileNameExtensionFilter filter = new FileNameExtensionFilter(".wav", "wav");
    private String audioTitle = "no audio opened";

    private final Hashtable<Integer, JLabel> hashtableEnd = new Hashtable<>();
    private final Hashtable<Integer, JLabel> hashtableFront = new Hashtable<>();

    JFileChooser fileChooser;

    public void init() {
        setSize(900, 600);
        setLocationRelativeTo(null);
        mainPanel.setLayout(mainLayout);
        this.add(mainPanel);

        selectedFile = new JLabel(audioTitle);
        selectedFile.setPreferredSize(new Dimension(800, 100));
        selectedFile.setFont(new Font("Verdana", Font.BOLD, 20));
        selectedFile.setAlignmentX(Component.CENTER_ALIGNMENT);

        cutEnd = new JSlider(0, 1000, 0);
        cutEnd.setPreferredSize(new Dimension(600, 40));
        cutEnd.setPaintLabels(true);
        currentCutOffEnd.setText("00:00:00");
        hashtableEnd.put(0, currentCutOffEnd);
        cutEnd.setLabelTable(hashtableEnd);
        cutEnd.setInverted(true);
        cutEnd.addChangeListener((change) -> updateFront());

        cutFront = new JSlider(0, 1000,0);
        cutFront.setPreferredSize(new Dimension(600, 40));
        cutFront.setPaintLabels(true);
        currentCutOffFront.setText("00:00:00");
        hashtableFront.put(0, currentCutOffFront);
        cutFront.setLabelTable(hashtableFront);
        cutFront.addChangeListener((change) -> updateEnd());

        JButton openFileExplorer = new JButton("open audio file");
        openFileExplorer.setAlignmentX(Component.CENTER_ALIGNMENT);
        openFileExplorer.addActionListener((change) -> openFileEvent());

        JButton exportFile = new JButton("export file");
        exportFile.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportFile.addActionListener((change) -> exportFileEvent());

        panelInner.setLayout(new BoxLayout(panelInner, BoxLayout.Y_AXIS));
        panelInner.add(Box.createRigidArea(new Dimension(0, 60)));
        panelInner.add(selectedFile);
        panelInner.add(Box.createRigidArea(new Dimension(0, 40)));
        panelInner.add(cutFront);
        panelInner.add(Box.createRigidArea(new Dimension(0, 20)));
        panelInner.add(cutEnd);
        panelInner.add(Box.createRigidArea(new Dimension(0, 80)));

        JPanel openAndExport = new JPanel();
        openAndExport.add(openFileExplorer);
        openAndExport.add(exportFile);
        panelInner.add(openAndExport);
        panelInner.add(Box.createRigidArea(new Dimension(0, 60)));

        /*JLabel error = new JLabel("please do not cut the whole audio");
        error.setForeground(Color.red);
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelInner.add(error);*/

        JPanel north = new JPanel();
        JPanel east = new JPanel();
        JPanel south = new JPanel();
        JPanel west = new JPanel();
        north.setBackground(Color.blue);
        south.setBackground(Color.green);
        mainPanel.add(panelInner, BorderLayout.CENTER);
        mainPanel.add(north, BorderLayout.NORTH);
        mainPanel.add(east, BorderLayout.EAST);
        mainPanel.add(south, BorderLayout.SOUTH);
        mainPanel.add(west, BorderLayout.WEST);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    // currently, not in use, but could be a better alternative
    private void openDirectory() {
        try {
            File directory = new File("C://Program Files//");
            Desktop.getDesktop().open(directory);
        } catch (IOException e) {
            System.out.println("Something went wrong opening the directory.");
        }
    }

    public void updateEnd() {
        cutEnd.setExtent(cutFront.getValue() + 1);
        if(fileInput != null)
            currentCutOffEnd.setText(getCutOffTextEnd());
    }

    public void updateFront() {
        cutFront.setExtent(cutEnd.getValue() + 1);
        if(fileInput != null)
            currentCutOffFront.setText(getCutOffTextFront());
    }

    private void updateFileName() {
        if(fileInput == null) {
            selectedFile.setText("unable to load audio");
        } else {
            audioTitle = fileInput.getName();
            selectedFile.setText(audioTitle);
        }
    }

    private String getCutOffTextEnd() {
        return getStandardTimeFormatFromSeconds(getSecondsCutOffEnd());
    }

    private String getCutOffTextFront() {
        return getStandardTimeFormatFromSeconds(getSecondsCutOffFront());
    }

    private long getSecondsCutOffEnd() {
        double currentRatio = cutEnd.getValue() / 1000.0;
        return (long) (AudioProcessor.getDurationInSeconds(fileInput) * currentRatio);
    }

    private long getSecondsCutOffFront() {
        double currentRatio = cutFront.getValue() / 1000.0;
        return (long) (AudioProcessor.getDurationInSeconds(fileInput) * currentRatio);
    }

    private String getStandardTimeFormatFromSeconds(long seconds) {
        short hours = (short) Math.floor(seconds / 60.0 / 60.0);
        short minutes = (short) Math.floor(seconds / 60.0);
        short secs = (short) (((seconds / 60.0) - Math.floor(seconds / 60.0)) * 60);
        String time = "";
        if(hours < 10) {
            time = time + "0" + hours + ":";
        } else {
            time = time + hours + ":";
        }
        if(minutes < 10) {
            time = time + "0" + minutes + ":";
        } else {
            time = time + minutes + ":";
        }
        if(secs < 10) {
            time = time + "0" + secs;
        } else {
            time = time + secs;
        }
        return time;
    }

    private void openFileEvent() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int response = fileChooser.showOpenDialog(null);
        if(response == JFileChooser.APPROVE_OPTION) {
            fileInput = new File(fileChooser.getSelectedFile().getAbsolutePath());
            if(AudioProcessor.getDurationInSeconds(fileInput) >= 360000) {
                fileInput = null;
            } else {
                updateFileName();
                System.out.println("loaded: " + fileInput.getName());
            }
        }
    }

    private void exportFileEvent() {
        if(fileInput != null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileFilter(filter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int response = fileChooser.showSaveDialog(null);
            if(response == JFileChooser.APPROVE_OPTION) {
                File fileOutput = new File(fileChooser.getSelectedFile().getAbsolutePath() + ".wav");
                AudioProcessor.shortenAudio(fileInput, (int) getSecondsCutOffFront()
                        ,(int)(AudioProcessor.getDurationInSeconds(fileInput) - getSecondsCutOffEnd()), fileOutput);
            }
        }
    }
}
