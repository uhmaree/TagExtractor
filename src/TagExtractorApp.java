import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TagExtractorApp extends JFrame {
    private final JTextField textFileField = new JTextField();
    private final JTextField stopFileField = new JTextField();
    private final JTextArea outputArea = new JTextArea();
    private final JButton chooseTextBtn = new JButton("Choose Text File");
    private final JButton chooseStopBtn = new JButton("Choose Stop Words");
    private final JButton runBtn = new JButton("Extract Tags");
    private final JButton saveBtn = new JButton("Save Output");
    private final JLabel statusLabel = new JLabel("Ready.");

    private Path textPath;
    private Path stopPath;
    private final TextTagExtractor extractor = new TextTagExtractor();

    public TagExtractorApp() {
        super("Tag/Keyword Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textFileField.setEditable(false);
        stopFileField.setEditable(false);

        JPanel top = new JPanel(new GridLayout(2, 3, 8, 8));
        top.setBorder(new EmptyBorder(10, 10, 10, 10));
        top.add(new JLabel("Text file:"));
        top.add(textFileField);
        top.add(chooseTextBtn);
        top.add(new JLabel("Stop words file:"));
        top.add(stopFileField);
        top.add(chooseStopBtn);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(runBtn);
        actions.add(saveBtn);

        JScrollPane scroll = new JScrollPane(outputArea);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(actions, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.PAGE_END);

        // Actions
        chooseTextBtn.addActionListener(this::chooseTextFile);
        chooseStopBtn.addActionListener(this::chooseStopFile);
        runBtn.addActionListener(this::runExtraction);
        saveBtn.addActionListener(this::saveOutput);
    }

    private void chooseTextFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textPath = chooser.getSelectedFile().toPath();
            textFileField.setText(textPath.toString());
            statusLabel.setText("Selected text file.");
        }
    }

    private void chooseStopFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            stopPath = chooser.getSelectedFile().toPath();
            stopFileField.setText(stopPath.toString());
            statusLabel.setText("Selected stop words file.");
        }
    }

    private void runExtraction(ActionEvent e) {
        if (textPath == null || stopPath == null) {
            JOptionPane.showMessageDialog(this, "Please select both files first.", "Missing files", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            extractor.loadStopWords(stopPath);
            Map<String, Integer> freq = extractor.extractTags(textPath);
            List<Map.Entry<String, Integer>> sorted = extractor.sortByFrequency(freq);
            String out = extractor.formatResults(sorted);
            outputArea.setText(out);
            statusLabel.setText("Extraction complete. " + sorted.size() + " keywords.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Extraction failed", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Failed.");
        }
    }

    private void saveOutput(ActionEvent e) {
        if (outputArea.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "No output to save.", "Empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save tag frequencies");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                extractor.saveOutput(chooser.getSelectedFile().toPath(), outputArea.getText());
                statusLabel.setText("Saved output.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Save failed", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Save failed.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TagExtractorApp().setVisible(true));
    }
}