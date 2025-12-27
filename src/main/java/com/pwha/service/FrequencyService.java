package com.pwha.service;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;
import com.pwha.util.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class responsible for the Analysis Phase of the algorithm.
 * <p>
 * This class processes words received from the {@code ByteReader}.
 * Its primary role is to identify the "Context" (first letter) of each word
 * and perform "Pattern Mining" on the remaining characters to populate the frequency maps.
 */
public class FrequencyService {

    // The main data structure holding all Contexts (keys) and their corresponding ContextLeaf nodes.
    // This acts as the "Super Frequency Map".
    private final HashMap<Byte,ContextLeaf> frequencyMap;

    public FrequencyService() {
        this.frequencyMap = new HashMap<>();
    }

    /**
     * Processes a single word (byte array) to update frequency statistics.
     * Logic:
     * 1. Identifies the Context (First Letter).
     * 2. Updates or creates the Context Node in the global map.
     * 3. Extracts all valid sub-patterns from the rest of the word.
     *
     * @param word The byte array representing a single word.
     */
    public void processWord(byte[] word) {
        if(word == null || word.length == 0) return;

        // First, we identify the 'Context', which is the first letter of the word.
        byte contextSymbol = word[0];
        ContextLeaf contextNode;

        if(frequencyMap.containsKey(contextSymbol)) {
            // If this Context already exists in our global map, retrieve it.
            contextNode = frequencyMap.get(contextSymbol);

            // Increment the frequency of this Context (first letter) itself.
            // This counts how many times words starting with this letter appear.
            contextNode.increaseFreqByOne();
        }else{
            // If this Context is not in the map, create a new ContextLeaf for it.
            contextNode = new ContextLeaf(contextSymbol, 1);

            // Register the new Context in the global map.
            frequencyMap.put(contextSymbol, contextNode);
        }

        // Delegate to the pattern mining method to process the rest of the word.
        extractAndAddPatterns(contextNode, word);
    }

    /**
     * Core Pattern Mining Logic.
     * Generates all valid substrings (patterns) from the word starting from the 2nd character.
     * <p>
     * Example: For word "them" (Context 't'):
     * - Generates: "h", "he", "hem", "e", "em", "m" ...
     * - Limits pattern length using Constant.MAX_PATTERN_LENGTH.
     */
    private void extractAndAddPatterns(ContextLeaf contextNode, byte[] word){
        // Start from index 1 because index 0 is the Context itself.
        for(int start = 1; start <word.length; start++){

            // Calculate the maximum end index based on the allowable pattern length.
            // This prevents generating patterns that are too long to be efficient.
            int maxEnd = Math.min(word.length - 1, start + Constant.MAX_PATTERN_LENGTH -1);

            // Extract substrings ranging from 'start' to 'end'.
            for(int end = maxEnd; end >= start; end--){
                byte[] pattern = Arrays.copyOfRange(word, start, end + 1);

                // Add the extracted pattern to the specific Context's sub-frequency map.
                contextNode.addToFreqMap(pattern);
            }
        }
    }

    /**
     * Associates a separator (like space, comma, dot) with the preceding Context.
     * This captures the relationship between words and the punctuation that follows them.
     */
    public void addSeparatorToContext(byte contextSymbol, byte separator){
        if(frequencyMap.containsKey(contextSymbol)){
            ContextLeaf contextNode = frequencyMap.get(contextSymbol);

            // Treat the separator as a single-byte pattern belonging to this context.
            contextNode.addToFreqMap(new byte[]{separator});
        }
    }

    public HashMap<Byte,ContextLeaf> getFrequencyMap() {
        return frequencyMap;
    }
}
