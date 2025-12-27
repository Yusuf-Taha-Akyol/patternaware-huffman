package com.pwha.engine;

import com.pwha.io.BitWriter;
import com.pwha.model.ByteArrayWrapper;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.util.SeparatorUtils;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * The Engine responsible for the Compression process.
 * <p>
 * This class handles the core encoding logic of the Pattern-Aware Huffman algorithm.
 * Key Steps:
 * 1. Writes the Dictionary (Frequency Map) to the file header.
 * 2. Reads the input file to identify words and separators.
 * 3. Encodes each word using "Context-Aware Greedy Matching".
 */
public class Encoder {
    private final HNode root;
    private final Map<Byte, ContextLeaf> dictionary;

    public Encoder(HNode root, Map<Byte, ContextLeaf> dictionary) {
        this.root = root;
        this.dictionary = dictionary;
    }

    /**
     * Main entry point for compression.
     * Sets up file streams (Buffered I/O) and triggers the encoding process.
     *
     * @param inputFile   Path to the source file.
     * @param outputFile  Path to the destination compressed file.
     * @param totalSize   Total size of the input file (used for progress calculation).
     * @param onProgress  Callback function to report progress percentage.
     */
    public void compress(String inputFile, String outputFile, long totalSize, Consumer<Double> onProgress) throws IOException {
        System.out.println("Compressing and File Writing");

        // Use Buffered streams for performance optimization (minimizes disk I/O).
        // BitWriter is used to write variable-length bits (Huffman codes) to the stream.
        try(FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            BitWriter bitWriter = new BitWriter(bos)) {

            // Step 1: Write the Header (Dictionary) so the decoder can rebuild the tree.
            writeHeader(fos);

            // Step 2: Encode the actual content of the file.
            encodeContent(inputFile, bitWriter, totalSize, onProgress);
        }

        System.out.println("Compressing complete... " + outputFile);
    }

    // Overloaded method for simple compression calls without progress tracking.
    public void compress(String inputFile, String outputFile) throws IOException {
        compress(inputFile, outputFile, 1, null);
    }

    /**
     * Serializes the Dictionary (Frequency Map) to the beginning of the file.
     * This creates a self-contained compressed file that holds its own decoding key.
     */
    private void writeHeader(OutputStream outputFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputFile);
        oos.writeObject(this.dictionary);
        oos.flush();
    }

    /**
     * Reads the input file byte-by-byte and processes it into "Words".
     * <p>
     * Logic:
     * - Accumulates bytes into a buffer until a separator (space, dot, etc.) is found.
     * - Encodes the accumulated word.
     * - Encodes the separator (checking if it belongs to the previous word's context).
     */
    private void encodeContent(String inputFile, BitWriter bitWriter, long totalSize, Consumer<Double> onProgress) throws IOException {

        try(FileInputStream fis = new FileInputStream(inputFile);
            BufferedInputStream bis = new BufferedInputStream(fis)){

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int data;
            long bytesReadSoFar = 0;

            // Read the file byte by byte until End of Stream (-1).
            while((data = bis.read()) != -1) {
                bytesReadSoFar++;

                // Report progress every 10KB.
                if(onProgress != null && bytesReadSoFar % 10240 == 0) {
                    double percent = (double) bytesReadSoFar / totalSize * 100;
                    onProgress.accept(percent);
                }

                byte byteValue = (byte) data;

                // If the current byte is a separator, it marks the end of a word.
                if (SeparatorUtils.isSeparator(byteValue)) {
                    // 1. Encode the accumulated word (if any).
                    if(bos.size() > 0){
                        encodeWord(bos.toByteArray(), bitWriter);
                    }

                    // 2. Handle the separator logic.
                    if(bos.size() > 0){
                        // Determine the context (first letter) of the word we just finished.
                        byte contextByte = bos.toByteArray()[0];
                        ContextLeaf contextLeaf = this.dictionary.get(contextByte);

                        String localSepCode = null;
                        if(contextLeaf != null) {
                            // Check if this specific separator exists as a pattern in the word's context.
                            // (e.g., does "word" + " " occur often enough to have a specific code?)
                            localSepCode = contextLeaf.getSubCode(new ByteArrayWrapper(new byte[]{byteValue}));
                        }

                        if(localSepCode != null){
                            bitWriter.writeBits(localSepCode);
                        }else{
                            // Fallback: Encode using the global dictionary if not found in context.
                            encodeGlobalSeparator(byteValue, bitWriter);
                        }
                    } else {
                        // If there was no preceding word (e.g., file starts with a space), use global encoding.
                        encodeGlobalSeparator(byteValue, bitWriter);
                    }
                    bos.reset(); // Clear the buffer for the next word.
                }else{
                    // If not a separator, add the byte to the current word buffer.
                    bos.write(byteValue);
                }
            }

            // Encode any remaining bytes in the buffer after the loop finishes.
            if(bos.size() > 0){
                encodeWord(bos.toByteArray(), bitWriter);
            }

        }

    }

    /**
     * Encodes a single word using the Pattern-Aware Logic.
     * Structure: [Context Code] + [Greedy Pattern Codes...]
     */
    private void encodeWord(byte[] word, BitWriter bitWriter) throws IOException {
        if(word.length == 0){return;}

        // 1. Identify Context (First letter).
        byte contextByte = word[0];

        ContextLeaf contextNode = this.dictionary.get(contextByte);

        if(contextNode == null){
            throw new IOException("Dictionary match failed for byte : " + contextByte);
        }

        // Write the Huffman code for the Context itself (Upper Layer).
        bitWriter.writeBits(contextNode.getCode());

        // 2. Encode the rest of the word using Greedy Matching (Lower Layer).
        if(word.length > 1){
            byte[] remain =  Arrays.copyOfRange(word, 1, word.length);
            processGreedyMatch(remain,  bitWriter, contextNode);
        }
    }

    /**
     * Encodes a separator globally when it cannot be found within a specific context.
     */
    private void encodeGlobalSeparator(byte separator, BitWriter bitWriter) throws IOException {
        ContextLeaf globalSepNode = this.dictionary.get(separator);
        if(globalSepNode != null){
            bitWriter.writeBits(globalSepNode.getCode());
        } else {
            System.out.println("Char couldn't found in Map : " + (char) separator);
        }
    }

    /**
     * * THE CORE ALGORITHM: Greedy Match Strategy *
     * Iterates through the byte array and tries to find the LONGEST matching pattern
     * available in the current Context's sub-tree.
     * <p>
     * Example: Input "ther", Context 't'. Remaining: "her".
     * - Tries "her" -> No match.
     * - Tries "he"  -> Match found! Write code for "he".
     * - Advances start index, processes remaining "r".
     */
    private void processGreedyMatch(byte[] byteValue, BitWriter bitWriter, ContextLeaf contextNode) throws IOException {
        int start = 0;
        while (start < byteValue.length) {
            boolean found = false;

            // Greedy Loop: Start checking from the longest possible substring (end) down to the shortest.
            for(int end = byteValue.length; end > start; end--){
                byte[] sub = Arrays.copyOfRange(byteValue, start, end);
                ByteArrayWrapper key = new ByteArrayWrapper(sub);

                // Check if this pattern has a code in the sub-tree.
                String code = contextNode.getSubCode(key);

                if(code != null){
                    // Match found! Write bits and advance the start pointer.
                    bitWriter.writeBits(code);
                    start = end;
                    found = true;
                    break;
                }
            }

            // If no pattern matches (should not happen if single characters are in the dict), skip byte.
            if(!found){
                start++;
            }
        }
    }
}
