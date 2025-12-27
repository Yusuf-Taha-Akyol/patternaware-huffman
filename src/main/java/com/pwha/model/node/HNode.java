package com.pwha.model.node;

import java.io.Serializable;

/**
 * Base abstract class for all nodes in the Huffman Tree.
 * <p>
 * This class represents the fundamental building block of the hierarchical tree structure used
 * in the Pattern-Aware Huffman Compression algorithm.
 * <p>
 * Key Features:
 * 1. **Comparable:** Implements comparison logic based on frequency for use in Priority Queues.
 * 2. **Serializable:** Allows the tree structure to be saved/loaded (e.g., for writing the Dictionary to the file header).
 * 3. **Tree Navigation:** Holds references to parent, left, and right child nodes.
 */
public abstract class HNode implements Comparable<HNode>, Serializable {

    // The frequency (occurrence count) of the character or pattern this node represents.
    // Lower frequency means higher priority in the Min-Queue during tree construction.
    protected int frequency;

    // Reference to the parent node. Useful for traversing up the tree if needed.
    private HNode parent;

    // Reference to the left child (representing bit '0').
    private HNode left;

    // Reference to the right child (representing bit '1').
    private HNode right;

    /**
     * Constructor to initialize a node with a specific frequency.
     * @param frequency The count of how many times the symbol/pattern appears.
     */
    public HNode(int frequency) {
        this.frequency = frequency;
    }

    // Default constructor.
    public HNode() {}

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public HNode getParent() {
        return parent;
    }

    public void setParent(HNode parent) {
        this.parent = parent;
    }

    public HNode getLeft() {
        return left;
    }

    public void setLeft(HNode left) {
        this.left = left;
    }

    public HNode getRight() {
        return right;
    }

    public void setRight(HNode right) {
        this.right = right;
    }

    /**
     * Abstract method to get the length of the pattern stored in this node.
     * <p>
     * Used for tie-breaking in the Priority Queue:
     * If two nodes have the same frequency, we may prefer longer or shorter patterns
     * depending on the optimization strategy.
     */
    public abstract int getPatternLength();

    /**
     * Checks if this node is a Leaf Node (contains data) or an Internal Node (structural).
     * @return true if it is a leaf, false otherwise.
     */
    public abstract boolean isLeaf();

    /**
     * Checks if this node specifically represents a 'Context' (the Upper Layer of the tree).
     * @return true if it is a ContextLeaf.
     */
    public abstract boolean isContextLeaf();

    /**
     * Compares this node with another node to determine order in the Priority Queue.
     * <p>
     * Sorting Logic:
     * - Primary Sort: Frequency (Ascending). Smallest frequency comes first.
     * - This ensures the standard Huffman property where rare items get longer codes.
     */
    @Override
    public int compareTo(HNode other){
        return Integer.compare(this.frequency, other.frequency);
    }

}
