package com.pwha.model.node;

import com.pwha.model.ByteArrayWrapper;

/**
 * Represents a leaf node in the Lower Layer (Pattern Tree).
 * This node stores the actual byte sequence (pattern) that acts as the leaf data in the Huffman Tree.
 * <p>
 * Key Logic:
 * 1. Wraps the byte pattern (e.g., "tion" or "the") using {@link ByteArrayWrapper}.
 * 2. Implements specific comparison logic to organize the Priority Queue.
 */
public class SimpleLeaf extends HNode {

    // The actual pattern data wrapped for safe usage (handling byte[] equality).
    private ByteArrayWrapper pattern;

    public SimpleLeaf(ByteArrayWrapper pattern, int frequency) {
        super(frequency);
        this.pattern = pattern;
    }

    public ByteArrayWrapper getPattern() {
        return pattern;
    }

    public void setPattern(ByteArrayWrapper pattern) {
        this.pattern = pattern;
    }

    // Checks if this leaf holds a single character or a multi-character pattern.
    // Returns true if the pattern length is 1 or less.
    public boolean isChar(){
        return pattern.length() <= 1;
    }

    // Converts the wrapped byte array into a String.
    // Useful for debugging, logging, or visualizing the pattern content.
    public String convertString(){
        byte[] data = this.pattern.data(); // Access data from the record

        return new String(data);
    }

    @Override
    public int getPatternLength() {
        return pattern.length();
    }

    @Override
    public boolean isContextLeaf(){
        return false; // This is a SimpleLeaf (Pattern), not a ContextLeaf.
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    // Critical method for defining the order of nodes in the Priority Queue.
    // Determines which nodes are processed first during Huffman Tree construction.
    @Override
    public int compareTo(HNode other){
        // Step 1: Compare based on frequency.
        // The Min-Priority Queue prioritizes lower frequency nodes (Smallest comes first).
        int freqCompare = Integer.compare(this.getFrequency(), other.getFrequency());

        // If frequencies are different, return the result.
        // Negative: this node is smaller (higher priority).
        // Positive: this node is larger (lower priority).
        if(freqCompare != 0) {
            return freqCompare;
        }

        // Step 2: Tie-Breaking Strategy (If frequencies are equal).
        /*
         * When two nodes have the same frequency, the standard PriorityQueue might order them arbitrarily.
         * To ensure a deterministic and optimized tree structure, we use Pattern Length as a secondary sort key.
         *
         * Logic:
         * - Shorter patterns are considered 'smaller' -> Popped first from the queue.
         * - Longer patterns are considered 'larger' -> Stay in the queue longer (placed at the back).
         * * This strategy aims to optimize the resulting Huffman code lengths for specific pattern types.
         */
        return Integer.compare(this.getPatternLength(), other.getPatternLength());
    }
}
