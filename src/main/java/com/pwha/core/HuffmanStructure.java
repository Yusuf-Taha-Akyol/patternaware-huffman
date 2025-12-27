package com.pwha.core;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.InternalNode;
import com.pwha.util.CustomPriorityQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Core Algorithm Logic.
 * <p>
 * This class is responsible for constructing the hierarchical "Two-Layered Huffman Tree".
 * <p>
 * Architecture:
 * 1. **Super-Tree (Upper Layer):** Models the frequency of 'Contexts' (starting characters).
 * 2. **Sub-Tree (Lower Layer):** Models the frequency of 'Patterns' specific to each context.
 */
public class HuffmanStructure {

    /**
     * Initializes the Custom Priority Queue with ContextLeaf nodes.
     * <p>
     * Takes the frequency map generated during the Analysis Phase and populates
     * a Min-Priority Queue to start the Huffman Tree construction.
     *
     * @param freqMap The map of Byte -> ContextLeaf.
     * @return A priority queue containing all Context leaves, sorted by frequency.
     */
    public static CustomPriorityQueue<ContextLeaf> setQueue(Map<Byte, ContextLeaf> freqMap) {
        CustomPriorityQueue<ContextLeaf> pq = new CustomPriorityQueue<>();
        for(ContextLeaf contextNode : freqMap.values()){
            pq.add(contextNode);
        }

        return pq;
    }

    /**
     * Builds the SUPER-TREE (Upper Layer) of the Huffman structure.
     * <p>
     * Algorithm:
     * 1. Extracts the two nodes with the lowest frequency from the queue.
     * 2. **Critical Step (Lazy Building):** Before merging, it checks if the nodes are leaves.
     * If so, it triggers {@link #buildSubTree(ContextLeaf)} to build their internal pattern trees.
     * 3. Merges the two nodes into a new InternalNode.
     * 4. Inserts the new parent back into the queue.
     * 5. Repeats until only one node (the Root) remains.
     *
     * @param pq The priority queue initialized with ContextLeaf nodes.
     * @return The root node of the fully constructed Super-Tree.
     */
    public static HNode buildSuperTree(CustomPriorityQueue<ContextLeaf> pq) {
        CustomPriorityQueue<HNode> pqUpper = new CustomPriorityQueue<>();
        pqUpper.addAll(pq);

        while (pqUpper.size() > 1) {
            HNode left =  pqUpper.poll();
            HNode right = pqUpper.poll();

            // LAZY LOADING: Construct the sub-tree only when the context is being processed.
            // This ensures we don't build trees for contexts that might be optimized away or processed differently.
            if(left.isLeaf()) {
                buildSubTree((ContextLeaf) left);
            }
            if(right.isLeaf()) {
                buildSubTree((ContextLeaf) right);
            }

            InternalNode parentNode = new InternalNode(left, right);

            // Establish bi-directional parent-child relationships
            parentNode.setLeft(left);
            parentNode.setRight(right);
            left.setParent(parentNode);
            right.setParent(parentNode);

            pqUpper.add(parentNode);
        }

        return pqUpper.poll(); // Return the Root of the Super-Tree
    }

    /**
     * Builds the SUB-TREE (Lower Layer) for a specific Context.
     * <p>
     * This creates a mini-Huffman tree for the patterns (e.g., "the", "tion")
     * that belong to a specific context character (e.g., 't').
     *
     * @param contextNode The context leaf for which the sub-tree is being built.
     */
    public static void buildSubTree(ContextLeaf contextNode) {
        CustomPriorityQueue<HNode> pqSub = new CustomPriorityQueue<>();

        // Populate the queue with patterns (SimpleLeaves) belonging to this context.
        contextNode.setSubQueue();
        pqSub.addAll(contextNode.getSubQueue());

        // Memory Optimization: Clear the raw queue list immediately to free RAM.
        contextNode.clearSubQueue();

        // Standard Huffman Construction for the Sub-Tree
        while (pqSub.size() > 1) {
            HNode left = pqSub.poll();
            HNode right = pqSub.poll();

            InternalNode parentNode = new InternalNode(left, right);

            parentNode.setLeft(left);
            parentNode.setRight(right);
            left.setParent(parentNode);
            right.setParent(parentNode);

            pqSub.add(parentNode);
        }

        // Assign the root of this new Pattern Tree to the ContextLeaf.
        contextNode.setSubTreeRoot(pqSub.poll());
    }

    /**
     * Recursive function to generate binary codes by traversing the tree.
     * <p>
     * Traversal Logic:
     * - Going Left adds '0' to the code.
     * - Going Right adds '1' to the code.
     * <p>
     * When a ContextLeaf is reached, it saves the code and triggers code generation
     * for its internal Sub-Tree.
     *
     * @param node        The current node being visited.
     * @param currentCode The binary code accumulated so far.
     * @param dictionary  The map to store the generated codes.
     */
    public static void buildDictionary(HNode node, String currentCode, Map<Byte, ContextLeaf> dictionary) {
        if(node == null){return;}

        // Base Case: Reached a Context (Leaf of Super-Tree).
        if(node instanceof ContextLeaf contextNode){
            contextNode.setCode(currentCode);
            dictionary.put(contextNode.getData(), contextNode);

            // Delegate code generation to the sub-tree (Pattern Tree)
            contextNode.generateCode();
        }

        // Recursive Step: Traverse internal nodes.
        if(node instanceof InternalNode internalNode){
            buildDictionary(internalNode.getLeft(),currentCode + 0,dictionary);
            buildDictionary(internalNode.getRight(), currentCode + 1,dictionary);
        }

    }
}
