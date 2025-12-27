package com.pwha.engine;

import com.pwha.core.HuffmanStructure;
import com.pwha.io.BitReader;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.InternalNode;
import com.pwha.model.node.SimpleLeaf;
import com.pwha.util.SeparatorUtils;

import java.io.*;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * The Engine responsible for Decompressing (Decoding) the file.
 * <p>
 * This class handles the reverse process of the Encoder:
 * 1. Reads the serialized dictionary (Header) to reconstruct the Huffman Trees.
 * 2. Reads the compressed bit stream.
 * 3. Traverses the trees to decode characters and patterns.
 * 4. Implements "Dynamic Context Switching" to select the correct tree for each step.
 */
public class Decoder {

    // The reconstructed dictionary mapping starting characters to their ContextLeaf nodes.
    private HashMap<Byte, ContextLeaf> globalContextMap;

    // The root of the reconstructed Super-Tree (connecting all Contexts).
    private HNode globalTreeRoot;

    /**
     * Main entry point for decompression.
     * Handles file I/O, progress tracking, and orchestrates the decoding flow.
     *
     * @param compressedFile Path to the input .pwha file.
     * @param outputFile     Path where the decoded content will be saved.
     * @param totalSize      Size of the compressed file (for progress calculation).
     * @param onProgress     Callback for UI progress updates.
     */
    public void decompress(String compressedFile, String outputFile, long totalSize, Consumer<Double> onProgress) throws IOException , ClassNotFoundException {
        System.out.println("Decompressing " + compressedFile + " to " + outputFile);

        FileInputStream fis = new FileInputStream(compressedFile);

        // Use BufferedInputStream for efficient disk reading (System Optimization).
        BufferedInputStream bis = new BufferedInputStream(fis);

        // Step 1: Read the Header (Serialized Dictionary)
        // This object contains the Frequency Map needed to rebuild the tree.
        ObjectInputStream ois = new ObjectInputStream(bis);

        this.globalContextMap = (HashMap<Byte, ContextLeaf>) ois.readObject();

        System.out.println("Dictionary is downloaded. Huffman Tree is rebuilding...");

        // Step 2: Reconstruct the exact Huffman Tree structure in memory.
        rebuildAllTrees();

        // Custom InputStream wrapper to track bytes read for the progress bar.
        InputStream progressStream = new InputStream() {
            long bytesRead = 0;

            @Override
            public int read() throws IOException {
                int b = bis.read();
                if(b != -1) update();
                return b;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int n = bis.read(b, off, len);
                if(n != -1) update(n);
                return n;
            }

            private void update() { update(1); }

            private void update(int n) {
                bytesRead += n;
                // Update progress every 10KB
                if(onProgress != null && bytesRead % 10240 == 0){
                    double p = (double) bytesRead / totalSize * 100;
                    onProgress.accept(p > 100 ? 100 : p);
                }
            }
        };

        // Initialize BitReader to read the compressed data bit-by-bit.
        BitReader bitReader = new BitReader(progressStream);

        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        System.out.println("Decompressing starting...");

        // Step 3: Start the actual decoding loop.
        decodeContent(bitReader, bos);

        // Close resources
        bos.close();
        bitReader.close();
        System.out.println("Decompressing complete...");
    }

    // Overloaded method for simple decompression without progress tracking.
    public void decompress(String compressedFile, String outputFile) throws IOException , ClassNotFoundException {
        decompress(compressedFile, outputFile, 1, null);
    }

    /**
     * Reconstructs the entire Two-Layered Huffman Tree from the frequency map.
     * Since Huffman trees are deterministic, this results in the exact same tree used during compression.
     */
    private void rebuildAllTrees() {
        // 1. Build Sub-Trees (Pattern Trees) for every Context.
        for(ContextLeaf contextNode : globalContextMap.values()) {
            contextNode.setSubQueue();
            HuffmanStructure.buildSubTree(contextNode);
        }

        // 2. Build the Super-Tree (Context Tree) that connects all contexts.
        var globalQueue = HuffmanStructure.setQueue(globalContextMap);
        this.globalTreeRoot = HuffmanStructure.buildSuperTree(globalQueue);
    }

    /**
     * Decodes the compressed bit stream by traversing the trees.
     * Implements the logic to switch between the Super-Tree and Sub-Trees based on context.
     */
    private void decodeContent(BitReader bitReader, OutputStream os) throws IOException {
        ContextLeaf currentContext = null;

        while(true) {
            HNode currentNode;

            // Context Switching Logic:
            // If we have no context (start of file or after separator) -> Search Super-Tree.
            // If we have a context -> Search that context's Sub-Tree.
            if(currentContext == null || currentContext.getSubTreeRoot() == null) {
                currentNode = globalTreeRoot;
            }else {
                currentNode = currentContext.getSubTreeRoot();
            }

            // Tree Traversal Loop: Read bits until a leaf is found.
            while(!currentNode.isLeaf()) {
                int bit = bitReader.readBit();

                if(bit == -1) {
                    return; // End of file/stream
                }

                InternalNode internal = (InternalNode) currentNode;
                // '0' -> Go Left, '1' -> Go Right
                currentNode = (bit == 0) ? internal.getLeft() : internal.getRight();

                if(currentNode == null) {return;} // Should not happen in a valid tree
            }

            // Leaf Node Reached! Process the data.
            if(currentNode instanceof ContextLeaf) {
                // We found a "Context" character (e.g., the first letter of a word).
                ContextLeaf leaf = (ContextLeaf) currentNode;
                byte data = leaf.getData();
                os.write(data);

                // Update Context:
                // If the character is a separator (space, dot), reset context.
                // Otherwise, this character becomes the new context for the next pattern.
                if(SeparatorUtils.isSeparator(data)){
                    currentContext = null;
                } else{
                    currentContext  = leaf;
                }
            }else if(currentNode instanceof SimpleLeaf) {
                // We found a "Pattern" (sequence of bytes).
                SimpleLeaf leaf = (SimpleLeaf) currentNode;
                byte[] data = leaf.getPattern().data();
                os.write(data);

                // Check if the pattern contains a separator (usually single-byte patterns).
                if(data.length == 1 && SeparatorUtils.isSeparator(data[0])){
                    currentContext = null;
                }else{
                    // For multi-byte patterns, the context usually continues or logic can be extended here.
                }
            }
        }
    }
}