package com.pwha.model.node;

/**
 * Represents an internal node in the Huffman Tree.
 * <p>
 * Unlike leaf nodes, internal nodes do not hold actual data (characters or patterns).
 * Their sole purpose is to provide the structure of the tree, connecting child nodes
 * and holding the combined frequency of their sub-trees.
 * <p>
 * During traversal (Decompression):
 * - Moving Left corresponds to bit '0'.
 * - Moving Right corresponds to bit '1'.
 */
public class InternalNode extends HNode {

    /**
     * Constructs a new Internal Node by merging two existing nodes.
     * <p>
     * The frequency of this internal node is the sum of its children's frequencies.
     * This is the core logic of the Huffman algorithm: combining the two smallest probability nodes.
     *
     * @param leftNode  The left child (representing the '0' path).
     * @param rightNode The right child (representing the '1' path).
     */
    public InternalNode(HNode leftNode, HNode rightNode) {
        super(leftNode.getFrequency() + rightNode.getFrequency());
    }

    /**
     * Checks if this node is a Context Leaf.
     * Always returns false because Internal Nodes are structural, not context holders.
     */
    @Override
    public boolean isContextLeaf(){
        return false;
    }

    /**
     * Returns the pattern length.
     * Since Internal Nodes do not store patterns, this returns 0.
     * This method exists to satisfy the abstract contract in HNode.
     */
    @Override
    public int getPatternLength() {return 0;}

    /**
     * Checks if this is a leaf node.
     * Always returns false, as this node has children (it is a branch).
     */
    @Override
    public boolean isLeaf() {
        return false;
    }

}
