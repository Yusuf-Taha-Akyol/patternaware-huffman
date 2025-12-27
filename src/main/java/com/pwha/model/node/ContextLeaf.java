package com.pwha.model.node;

import com.pwha.model.ByteArrayWrapper;
import com.pwha.util.Constant;
import com.pwha.util.CustomPriorityQueue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Represents a 'Context' node in the Huffman Tree (Upper Layer).
 * <p>
 * This class corresponds to a starting character (Context) and manages the
 * sub-patterns associated with it. It acts as the root for the second layer
 * of the compression architecture.
 * <p>
 * Key Responsibilities:
 * 1. Stores frequency data for patterns starting with this character.
 * 2. Manages memory using an approximate 'Space-Saving' algorithm (Eviction).
 * 3. Builds and holds the specific Huffman Sub-Tree for this context.
 */
public class ContextLeaf extends HNode implements Serializable {

    // The actual character data for this context (e.g., 'a', 'b').
    private final byte data;

    // Map storing the frequency of patterns belonging to this context.
    // Key: The pattern (wrapped byte array), Value: Frequency count.
    private final HashMap<ByteArrayWrapper, Integer> freqMap;

    // The root of the generated Huffman Sub-Tree for this context.
    private transient HNode subTreeRoot;

    // The dictionary mapping patterns to their binary codes within this context.
    private transient final HashMap<ByteArrayWrapper, String> subDictionary;

    // Priority Queue used to build the sub-tree.
    private transient CustomPriorityQueue<SimpleLeaf> priorityQueue;

    // The Huffman code assigned to this Context node in the Super-Tree.
    private transient String code;
    private int kValue;

    // Constructor Method.
    public ContextLeaf(byte data, int frequency) {
        super(frequency);
        this.data = data;
        this.freqMap = new HashMap<>();
        this.subDictionary = new HashMap<>();
        this.priorityQueue = new CustomPriorityQueue<SimpleLeaf>();
    }

    // Getters and Setters
    public byte getData(){
        return this.data;
    }

    public HNode getSubTreeRoot(){
        return subTreeRoot;
    }

    public void setSubTreeRoot(HNode subTreeRoot) {
        this.subTreeRoot = subTreeRoot;
    }

    public CustomPriorityQueue<SimpleLeaf> getSubQueue(){
        return this.priorityQueue;
    }

    /**
     * Initializes the Priority Queue for the sub-tree construction.
     * Converts entries from the frequency map into SimpleLeaf nodes.
     */
    public void setSubQueue(){
        this.priorityQueue = new CustomPriorityQueue<>();
        for(ByteArrayWrapper key : freqMap.keySet()){
            this.priorityQueue.add(new SimpleLeaf(key, freqMap.get(key)));
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Adds a pattern to the sub-frequency map.
     * Handles limits on pattern length and total number of patterns (Memory Management).
     *
     * @param pattern The byte array representing the pattern to add.
     */
    // In here we are adding patterns and chars to the sub frequency map.
    public void addToFreqMap(byte[] pattern) {
        // We are checking pattern length. This optimizes the swelling of the header section.
        // If a pattern is too long, we ignore it to keep the dictionary size manageable.
        if(pattern.length > Constant.MAX_PATTERN_LENGTH){
            return;
        }

        // In here we are getting pattern or char value from sub frequency map if it contains in freq map.
        // We use ByteArrayWrapper to ensure correct hashing and equality checks for byte arrays.
        ByteArrayWrapper byteArrayWrapper = new ByteArrayWrapper(pattern);
        Integer count = freqMap.get(byteArrayWrapper);

        // Value exist in map we are just increasing freq value of this value.
        if(count != null){
            freqMap.put(byteArrayWrapper, count + 1);
            return;
        }

        // If value is a single char (length 1), then we are adding directly to the sub frequency map.
        // Single characters are base units and should generally not be evicted.
        if(byteArrayWrapper.length() == 1){
            freqMap.put(byteArrayWrapper, 1);
            return;
        }

        /*
        In here we are checking freq map size. This section is important for the Space-Save algorithm.
        If map size is less than MAX_PATTERN_AMOUNT, we simply add the new pattern.
        If the map is full, we trigger the eviction logic to make space.
         */
        if(freqMap.size() < Constant.MAX_PATTERN_AMOUNT){
            freqMap.put(byteArrayWrapper, 1);
            return;
        }

        // This method finds a candidate for removal (victim) and puts the new pattern into the map.
        evictSafeAndAdd(byteArrayWrapper);
    }

    /**
     * Implements an optimized Eviction Policy (Space-Saving).
     * Instead of a full scan (O(N)), it uses Randomized Sampling (O(1)) to find a low-frequency item to remove.
     *
     * @param byteArrayWrapper The new pattern to be added.
     */
    public void evictSafeAndAdd(ByteArrayWrapper byteArrayWrapper){
        ByteArrayWrapper victim = null;
        int minFreq = Integer.MAX_VALUE;

        /*
        In here for optimizing the time complexity we are not checking the entire freq map.
        We are just looking at 100 random elements in the map.
        And we are choosing the minimum value among them as a victim.
        So that maybe we are missing the absolute real minimum value in map but with that we are optimizing the time complexity to O(1).
         */
        int sampleSize = 100;
        int checked = 0;
        for(Map.Entry<ByteArrayWrapper, Integer> entry : freqMap.entrySet()){
            // Do not evict single characters (base context).
            if(entry.getKey().data().length == 1) continue;

            int val = entry.getValue();

            // Optimization: If we find an item with frequency 1, it's the best possible victim.
            if(val == 1){
                victim = entry.getKey();
                minFreq = val;
                break;
            }

            // Track the minimum frequency found in the sample.
            if(val < minFreq){
                minFreq = val;
                victim = entry.getKey();
            }

            checked++;
            if(checked >= sampleSize) break;
        }

        // If a victim is found, remove it and add the new pattern.
        if(victim != null){
            freqMap.remove(victim);
            freqMap.put(byteArrayWrapper, minFreq + 1);
        } else{
            // Fallback: If no victim found (rare case), force add if it is a single char.
            if(byteArrayWrapper.data().length == 1){
                freqMap.put(byteArrayWrapper, minFreq + 1);
            }
        }
    }

    /**
     * Clears the priority queue to free up memory after the tree is built.
     */
    public void clearSubQueue(){
        this.priorityQueue = null;
    }

    // This method for setting code for context word.
    // Triggers the recursive generation of binary codes for the entire sub-tree.
    public void generateCode() {
        subDictionary.clear();

        if(subTreeRoot != null){
            traverseAndBuildDictionary(subTreeRoot, "");
        }
    }

    // This method setting subtree dictionary. We will use this dictionary for coding.
    // Recursively traverses the tree: Left = "0", Right = "1".
    private void traverseAndBuildDictionary(HNode node, String currentCode){
        if(node == null) return;

        // If we reach a leaf (Pattern), store its code in the dictionary.
        if(node instanceof SimpleLeaf simpleNode){
            this.subDictionary.put(simpleNode.getPattern(), currentCode);
        }

        // If internal node, continue traversal.
        if(node instanceof InternalNode internalNode){
            traverseAndBuildDictionary(internalNode.getLeft(), currentCode + "0");
            traverseAndBuildDictionary(internalNode.getRight(), currentCode + "1");
        }
    }

    // For get sub elements sub Code.
    // Returns the binary code for a specific pattern under this context.
    public String getSubCode(ByteArrayWrapper key){
        return subDictionary.get(key);
    }

    // Frequency Management
    public void increaseFreqByOne() {
        setFrequency(getFrequency() + 1);
    }

    public int getPatternCount() {
        return freqMap.size();
    }

    @Override
    public boolean isContextLeaf(){
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getPatternLength() {return 1;}
}