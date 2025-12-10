package com.pwha.gui;

import com.pwha.core.HuffmanStructure;
import com.pwha.engine.Decoder;
import com.pwha.engine.Encoder;
import com.pwha.io.ByteReader;
import com.pwha.model.node.HNode;
import com.pwha.service.FrequencyService;
import com.pwha.util.Constant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class App extends JFrame {

    private JTextField filePathField;
    private JTextArea logArea;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton viewTreeButton;
    private JProgressBar progressBar;
    private File selectedFile;
    private HNode currentRoot;

    // Ayar bileşenleri
    private JSpinner patternLengthSpinner;
    private JSpinner patternAmountSpinner;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new App().setVisible(true);
        });
    }

    public App() {
        setTitle("Pattern-Aware Huffman Compressor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        JLabel fileLabel = new JLabel("Selected File:");
        filePathField = new JTextField();
        filePathField.setEditable(false);
        JButton browseButton = new JButton("Browse...");

        topPanel.add(fileLabel, BorderLayout.WEST);
        topPanel.add(filePathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        settingsPanel.setBorder(new TitledBorder("Algorithm Settings"));

        settingsPanel.add(new JLabel("Max Pattern Length:", SwingConstants.RIGHT));
        patternLengthSpinner = new JSpinner(new SpinnerNumberModel(20, 2, 100, 1));
        settingsPanel.add(patternLengthSpinner);

        settingsPanel.add(new JLabel("Max Pattern Amount:", SwingConstants.RIGHT));
        patternAmountSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 100000, 100));
        settingsPanel.add(patternAmountSpinner);

        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        centerContainer.add(settingsPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(logArea);

        centerContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true); // Yüzdeyi yazı olarak göster
        progressBar.setFont(new Font("Arial", Font.BOLD, 12));
        progressPanel.add(new JLabel("Progress: "), BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        centerContainer.add(progressPanel, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        compressButton = new JButton("Compress File");
        decompressButton = new JButton("Decompress File");
        viewTreeButton = new JButton("View Tree");

        compressButton.setEnabled(false);
        decompressButton.setEnabled(false);
        viewTreeButton.setEnabled(false);

        Dimension btnSize = new Dimension(140, 40);
        compressButton.setPreferredSize(btnSize);
        decompressButton.setPreferredSize(btnSize);
        viewTreeButton.setPreferredSize(btnSize);

        bottomPanel.add(compressButton);
        bottomPanel.add(decompressButton);
        bottomPanel.add(viewTreeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        browseButton.addActionListener(e -> chooseFile());
        compressButton.addActionListener(e -> startCompressionTask());
        decompressButton.addActionListener(e -> startDecompressionTask());
        viewTreeButton.addActionListener(e -> showTreeWindow());

        log("Welcome! Please select a file and configure settings.");
    }

    private void updateProgress(double percent) {
        SwingUtilities.invokeLater(() -> progressBar.setValue((int) percent));
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            compressButton.setEnabled(true);
            decompressButton.setEnabled(true);
            progressBar.setValue(0);
            log("File selected: " + selectedFile.getName() + " (" + formatSize(selectedFile.length()) + ")");
        }
    }

    private void startCompressionTask() {
        int pLength = (Integer) patternLengthSpinner.getValue();
        int pAmount = (Integer) patternAmountSpinner.getValue();
        Constant.MAX_PATTERN_LENGTH = pLength;
        Constant.MAX_PATTERN_AMOUNT = pAmount;

        new Thread(() -> {
            try {
                toggleButtons(false);
                updateProgress(0);
                String inputFile = selectedFile.getAbsolutePath();
                String outputFile = selectedFile.getParent() + File.separator + "encoded_file.pwha";
                long totalSize = selectedFile.length();

                log("------------------------------------------------");
                log("Starting Compression...");
                long startTime = System.currentTimeMillis();

                log("Stage 1: File Analyzing...");
                FrequencyService frequencyService = new FrequencyService();
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    ByteReader reader = new ByteReader(frequencyService, fis);
                    reader.collectWords(totalSize, progress -> updateProgress(progress * 0.5));
                }

                if (frequencyService.getFrequencyMap().isEmpty()) {
                    throw new RuntimeException("Frequency Map is empty!");
                }
                log("Analysis complete. Patterns: " + frequencyService.getFrequencyMap().size());

                log("Stage 2: Building Huffman Tree...");
                HNode root = HuffmanStructure.buildSuperTree(HuffmanStructure.setQueue(frequencyService.getFrequencyMap()));
                HuffmanStructure.buildDictionary(root, "", frequencyService.getFrequencyMap());

                this.currentRoot = root;
                SwingUtilities.invokeLater(() -> viewTreeButton.setEnabled(true));

                log("Stage 3: Compressing...");
                Encoder encoder = new Encoder(root, frequencyService.getFrequencyMap());
                encoder.compress(inputFile, outputFile, totalSize, progress -> updateProgress(50 + (progress * 0.5)));

                updateProgress(100);
                long endTime = System.currentTimeMillis();
                File originalFile = new File(inputFile);
                File compressedFile = new File(outputFile);
                double ratio = 100.0 * (1.0 - ((double) compressedFile.length() / originalFile.length()));

                log("------------------------------------------------");
                log("COMPRESSION SUCCESSFUL!");
                log(String.format("Time Taken: %.2f sec", (endTime - startTime) / 1000.0));
                log("Original: " + formatSize(originalFile.length()));
                log("Compressed: " + formatSize(compressedFile.length()));
                log(String.format("Efficiency: %.2f%% saved", ratio));

            } catch (Exception ex) {
                log("ERROR: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                toggleButtons(true);
            }
        }).start();
    }

    private void startDecompressionTask() {
        new Thread(() -> {
            try {
                toggleButtons(false);
                updateProgress(0);
                String inputFile = selectedFile.getAbsolutePath();
                String outputFile = inputFile.endsWith(".pwha")
                        ? inputFile.substring(0, inputFile.length() - 5) + "_decoded.txt"
                        : inputFile + "_decoded.txt";
                long totalSize = selectedFile.length();

                log("------------------------------------------------");
                log("Starting Decompression...");

                Decoder decoder = new Decoder();
                decoder.decompress(inputFile, outputFile, totalSize, this::updateProgress);

                updateProgress(100);
                log("SUCCESS! File restored to: " + new File(outputFile).getName());

            } catch (Exception ex) {
                log("ERROR: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                toggleButtons(true);
            }
        }).start();
    }

    private void showTreeWindow() {
        if (currentRoot == null) return;
        JFrame treeFrame = new JFrame("Huffman Tree Visualization");
        treeFrame.setSize(1200, 800);
        treeFrame.setLocationRelativeTo(this);
        treeFrame.setLayout(new BorderLayout());

        HuffmanTreePainter painter = new HuffmanTreePainter(currentRoot);
        JScrollPane scrollPane = new JScrollPane(painter);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("← Back to Main Tree");
        backButton.setEnabled(false);

        controlPanel.add(backButton);
        treeFrame.add(controlPanel, BorderLayout.NORTH);
        treeFrame.add(scrollPane, BorderLayout.CENTER);

        backButton.addActionListener(e -> {
            painter.resetToGlobal();
            backButton.setEnabled(false);
        });

        painter.setOnSubTreeSelected(() -> backButton.setEnabled(true));

        treeFrame.setVisible(true);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void toggleButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            compressButton.setEnabled(enabled);
            decompressButton.setEnabled(enabled);
            filePathField.setEnabled(enabled);
            patternLengthSpinner.setEnabled(enabled);
            patternAmountSpinner.setEnabled(enabled);
        });
    }

    private String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
