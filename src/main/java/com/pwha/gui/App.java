package com.pwha.gui;

import com.pwha.core.HuffmanStructure;
import com.pwha.engine.Decoder;
import com.pwha.engine.Encoder;
import com.pwha.io.ByteReader;
import com.pwha.model.node.HNode;
import com.pwha.service.FrequencyService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;

public class App extends JFrame {

    private HNode currentRoot;
    private JButton viewTreeButton;
    private JTextField filePathField;
    private JTextArea logArea;
    private JButton compressButton;
    private JButton decompressButton;
    private File selectedFile;

    public static void main(String[] args) {
        // Arayüzü "Event Dispatch Thread" üzerinde başlat
        SwingUtilities.invokeLater(() -> {
            try {
                // İşletim sistemi temasına uyum sağla (Windows/Mac görünümü)
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
        setSize(600, 450);
        setLocationRelativeTo(null); // Ekranın ortasında aç
        setLayout(new BorderLayout(10, 10));

        // --- ÜST PANEL (DOSYA SEÇİMİ) ---
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

        // --- ORTA PANEL (LOG EKRANI) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(logArea);

        centerPanel.add(new JLabel("Process Log:"), BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- ALT PANEL (BUTONLAR) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        compressButton = new JButton("Compress File");
        decompressButton = new JButton("Decompress File");
        viewTreeButton = new JButton("View Tree");

        // Butonları başta pasif yap (dosya seçilmedi)
        compressButton.setEnabled(false);
        decompressButton.setEnabled(false);
        viewTreeButton.setEnabled(false);

        // Buton boyutlarını ayarla
        Dimension btnSize = new Dimension(150, 40);
        compressButton.setPreferredSize(btnSize);
        decompressButton.setPreferredSize(btnSize);

        bottomPanel.add(compressButton);
        bottomPanel.add(decompressButton);
        bottomPanel.add(viewTreeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- AKSİYONLAR ---

        // Dosya Seçme İşlemi
        browseButton.addActionListener(e -> chooseFile());

        // Sıkıştırma İşlemi
        compressButton.addActionListener(e -> startCompressionTask());

        // Açma İşlemi
        decompressButton.addActionListener(e -> startDecompressionTask());

        viewTreeButton.addActionListener(e -> showTreeWindow());

        log("Welcome! Please select a file to start.");
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        // Varsayılan olarak proje klasörünü açsın
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            compressButton.setEnabled(true);
            decompressButton.setEnabled(true);
            log("File selected: " + selectedFile.getName() + " (" + (selectedFile.length() / 1024) + " KB)");
        }
    }

    // Arayüz donmasın diye işlemleri ayrı bir Thread içinde yapıyoruz
    private void startCompressionTask() {
        new Thread(() -> {
            try {
                toggleButtons(false);
                String inputFile = selectedFile.getAbsolutePath();
                String outputFile = selectedFile.getParent() + File.separator + "encoded_file.pwha";

                log("------------------------------------------------");
                log("Starting Compression...");

                // Başlangıç zamanını tut (Süre hesabı için)
                long startTime = System.currentTimeMillis();

                // 1. Aşama: Analiz
                log("Stage 1: File Analyzing...");
                FrequencyService frequencyService = new FrequencyService();
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    ByteReader reader = new ByteReader(frequencyService, fis);
                    reader.collectWords();
                }

                if (frequencyService.getFrequencyMap().isEmpty()) {
                    throw new RuntimeException("Frequency Map is empty! Input file might be empty.");
                }
                log("Analysis complete. Unique patterns found: " + frequencyService.getFrequencyMap().size());

                // 2. Aşama: Huffman Ağacı
                log("Stage 2: Building Huffman Structure...");
                HNode root = HuffmanStructure.buildSuperTree(HuffmanStructure.setQueue(frequencyService.getFrequencyMap()));
                HuffmanStructure.buildDictionary(root, "", frequencyService.getFrequencyMap());

                // Ağacı görselleştirici için kaydet ve butonu aç
                this.currentRoot = root;
                SwingUtilities.invokeLater(() -> viewTreeButton.setEnabled(true));

                // 3. Aşama: Sıkıştırma
                log("Stage 3: Writing to " + new File(outputFile).getName());
                Encoder encoder = new Encoder(root, frequencyService.getFrequencyMap());
                encoder.compress(inputFile, outputFile);

                // --- SONUÇ RAPORU (YENİ KISIM) ---
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                File originalFile = new File(inputFile);
                File compressedFile = new File(outputFile);

                long origSize = originalFile.length();
                long compSize = compressedFile.length();

                // Yüzde hesabı
                double ratio = 100.0 * (1.0 - ((double) compSize / origSize));

                log("------------------------------------------------");
                log("COMPRESSION SUCCESSFUL!");
                log(String.format("Time Taken: %.2f seconds", duration / 1000.0));
                log("Original Size:   " + formatSize(origSize));
                log("Compressed Size: " + formatSize(compSize));
                log(String.format("Efficiency:      %.2f%% space saved", ratio));
                log("Output: " + outputFile);

            } catch (Exception ex) {
                log("ERROR: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                toggleButtons(true);
            }
        }).start();
    }

    private String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    private void startDecompressionTask() {
        new Thread(() -> {
            try {
                toggleButtons(false);
                String inputFile = selectedFile.getAbsolutePath();
                // .pwha uzantısını kaldırıp .decoded.txt ekleyelim
                String outputFile = inputFile.endsWith(".pwha")
                        ? inputFile.substring(0, inputFile.length() - 5) + "_decoded.txt"
                        : inputFile + "_decoded.txt";

                log("------------------------------------------------");
                log("Starting Decompression...");
                log("Input: " + selectedFile.getName());
                log("Output Target: " + new File(outputFile).getName());

                Decoder decoder = new Decoder();
                decoder.decompress(inputFile, outputFile);

                log("SUCCESS! Decompression finished.");

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
        if(currentRoot == null) return;

        JFrame treeFrame = new JFrame("Huffman Tree Visualization");
        treeFrame.setSize(1200,800);
        treeFrame.setLocationRelativeTo(this);
        treeFrame.setLayout(new BorderLayout());

        HuffmanTreePainter painter = new HuffmanTreePainter(currentRoot);
        JScrollPane scrollPane = new JScrollPane(painter);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("<- Back to Main Tree");
        backButton.setEnabled(false);

        controlPanel.add(backButton);
        treeFrame.add(controlPanel, BorderLayout.NORTH);
        treeFrame.add(scrollPane, BorderLayout.CENTER);

        backButton.addActionListener(e -> {
            painter.resetToGlobal();
            backButton.setEnabled(false);
        });

        painter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (painter.isShowingSubTree()){
                    backButton.setEnabled(true);
                }
            }
        });

        treeFrame.setVisible(true);
    }

    // Helper: Log alanına yazı yaz
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Otomatik en alta kaydır
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // Helper: İşlem sırasında butonları kilitle
    private void toggleButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            compressButton.setEnabled(enabled);
            decompressButton.setEnabled(enabled);
            filePathField.setEnabled(enabled);
        });
    }
}
