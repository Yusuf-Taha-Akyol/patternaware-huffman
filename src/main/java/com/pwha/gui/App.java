package com.pwha.gui;

import com.pwha.core.HuffmanStructure;
import com.pwha.engine.Decoder;
import com.pwha.engine.Encoder;
import com.pwha.io.ByteReader;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.service.FrequencyService;
import com.pwha.util.Constant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Main Entry Point and GUI for the Pattern-Aware Huffman Compressor.
 * <p>
 * This class extends JFrame to provide a user-friendly Swing interface.
 * It handles file selection, parameter configuration (pattern length/amount),
 * and orchestrates the Compression and Decompression tasks on background threads.
 */
public class App extends JFrame {

    // GUI Components
    private JTextField filePathField;
    private JTextArea logArea;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton viewTreeButton;
    private JProgressBar progressBar;

    // Application State
    private File selectedFile;
    private HNode currentRoot; // Stores the root of the generated Huffman Tree for visualization

    // Settings Components
    private JSpinner patternLengthSpinner;
    private JSpinner patternAmountSpinner;

    /**
     * Main method to launch the application.
     * Runs the GUI construction on the Event Dispatch Thread (EDT).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the Look and Feel to match the operating system
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new App().setVisible(true);
        });
    }

    /**
     * Constructor: Initializes the GUI layout and components.
     */
    public App() {
        setTitle("Pattern-Aware Huffman Compressor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel: File Selection ---
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

        // --- Settings Panel: Algorithm Configuration ---
        JPanel settingsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        settingsPanel.setBorder(new TitledBorder("Algorithm Settings"));

        // Spinner for MAX_PATTERN_LENGTH
        settingsPanel.add(new JLabel("Max Pattern Length:", SwingConstants.RIGHT));
        patternLengthSpinner = new JSpinner(new SpinnerNumberModel(20, 2, 100, 1));
        settingsPanel.add(patternLengthSpinner);

        // Spinner for MAX_PATTERN_AMOUNT
        settingsPanel.add(new JLabel("Max Pattern Amount:", SwingConstants.RIGHT));
        patternAmountSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 100000, 10));
        settingsPanel.add(patternAmountSpinner);

        // --- Center Panel: Logs and Progress ---
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        centerContainer.add(settingsPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(logArea);

        centerContainer.add(scrollPane, BorderLayout.CENTER);

        // Progress Bar Panel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true); // Show percentage text
        progressBar.setFont(new Font("Arial", Font.BOLD, 12));
        progressPanel.add(new JLabel("Progress: "), BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        centerContainer.add(progressPanel, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);

        // --- Bottom Panel: Action Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        compressButton = new JButton("Compress File");
        decompressButton = new JButton("Decompress File");
        viewTreeButton = new JButton("View Tree");

        // Disable buttons until a file is selected
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

        // Event Listeners
        browseButton.addActionListener(e -> chooseFile());
        compressButton.addActionListener(e -> startCompressionTask());
        decompressButton.addActionListener(e -> startDecompressionTask());
        viewTreeButton.addActionListener(e -> showTreeWindow());

        log("Welcome! Please select a file and configure settings.");
    }

    /**
     * Updates the progress bar on the Event Dispatch Thread.
     */
    private void updateProgress(double percent) {
        SwingUtilities.invokeLater(() -> progressBar.setValue((int) percent));
    }

    /**
     * Opens a file chooser dialog to select the input file.
     */
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

    /**
     * Starts the Compression process in a background thread.
     * Flow: Analysis -> Tree Building -> Compression.
     */
    private void startCompressionTask() {
        // Apply settings from GUI
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

                // Phase 1: Analysis (Pattern Mining)
                log("Stage 1: File Analyzing...");
                FrequencyService frequencyService = new FrequencyService();
                try (FileInputStream fis = new FileInputStream(inputFile);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {

                    ByteReader reader = new ByteReader(frequencyService, bis);
                    // Update progress up to 50% during analysis
                    reader.collectWords(totalSize, progress -> updateProgress(progress * 0.5));
                }

                if (frequencyService.getFrequencyMap().isEmpty()) {
                    throw new RuntimeException("Frequency Map is empty!");
                }

                // Log statistics
                int totalContexts = frequencyService.getFrequencyMap().size();
                long totalPatterns = 0;
                for(ContextLeaf leaf : frequencyService.getFrequencyMap().values()) {
                    totalPatterns += leaf.getPatternCount();
                }
                log("Analysis complete.");
                log(" - Total contexts : " + totalContexts);
                log(" - Total patterns : " + totalPatterns);

                // Phase 2: Huffman Tree Construction
                log("Stage 2: Building Huffman Tree...");
                HNode root = HuffmanStructure.buildSuperTree(HuffmanStructure.setQueue(frequencyService.getFrequencyMap()));
                HuffmanStructure.buildDictionary(root, "", frequencyService.getFrequencyMap());

                // Enable Tree Visualization
                this.currentRoot = root;
                SwingUtilities.invokeLater(() -> viewTreeButton.setEnabled(true));

                // Phase 3: Encoding (Writing to file)
                log("Stage 3: Compressing...");
                Encoder encoder = new Encoder(root, frequencyService.getFrequencyMap());
                // Update progress from 50% to 100% during encoding
                encoder.compress(inputFile, outputFile, totalSize, progress -> updateProgress(50 + (progress * 0.5)));

                updateProgress(100);
                long endTime = System.currentTimeMillis();
                File originalFile = new File(inputFile);
                File compressedFile = new File(outputFile);
                double ratio = 100.0 * (1.0 - ((double) compressedFile.length() / originalFile.length()));

                // Summary Log
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

    /**
     * Starts the Decompression process in a background thread.
     */
    private void startDecompressionTask() {
        new Thread(() -> {
            try {
                toggleButtons(false);
                updateProgress(0);
                String inputFile = selectedFile.getAbsolutePath();
                // Determine output filename
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

    /**
     * Opens the visual Tree Viewer window.
     * Displays the generated Huffman Tree and allows zooming/navigation.
     */
    private void showTreeWindow() {
        if (currentRoot == null) return;
        JFrame treeFrame = new JFrame("Huffman Tree Visualization");
        treeFrame.setSize(1200, 800);
        treeFrame.setLocationRelativeTo(this);
        treeFrame.setLayout(new BorderLayout());

        HuffmanTreePainter painter = new HuffmanTreePainter(currentRoot);
        JScrollPane scrollPane = new JScrollPane(painter);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Back Button (for navigating out of sub-trees)
        JButton backButton = new JButton("← Back to Main Tree");
        backButton.setEnabled(false);

        // Zoom Controls
        JLabel zoomLabel = new JLabel("Zoom Level:");
        // Start: 1.0, Min: 0.2, Max: 3.0, Step: 0.1
        JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(1.0, painter.MIN_ZOOM, painter.MAX_ZOOM, 0.1));

        // Update zoom when spinner changes
        zoomSpinner.addChangeListener(e -> {
            double val = (Double) zoomSpinner.getValue();
            painter.setScaleFactor(val);
        });

        // Add components to panel
        controlPanel.add(backButton);
        controlPanel.add(Box.createHorizontalStrut(20)); // Spacer
        controlPanel.add(zoomLabel);
        controlPanel.add(zoomSpinner);

        treeFrame.add(controlPanel, BorderLayout.NORTH);
        treeFrame.add(scrollPane, BorderLayout.CENTER);

        // Reset to global tree view
        backButton.addActionListener(e -> {
            painter.resetToGlobal();
            backButton.setEnabled(false);
        });

        // Enable back button when user dives into a sub-tree
        painter.setOnSubTreeSelected(() -> backButton.setEnabled(true));

        treeFrame.setVisible(true);
    }

    /**
     * Helper method to append messages to the log area safely on the EDT.
     */
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }

    /**
     * Helper method to enable/disable UI buttons during processing.
     */
    private void toggleButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            compressButton.setEnabled(enabled);
            decompressButton.setEnabled(enabled);
            filePathField.setEnabled(enabled);
            patternLengthSpinner.setEnabled(enabled);
            patternAmountSpinner.setEnabled(enabled);
        });
    }

    /**
     * Formats bytes into human-readable units (KB, MB, GB).
     */
    private String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
