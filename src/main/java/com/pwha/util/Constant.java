package com.pwha.util;
import java.util.HashSet;
import java.util.Set;

/**
 * Global Constants configuration class.
 * <p>
 * This class serves as the central configuration file for the algorithm.
 * It defines critical limits and settings that control:
 * 1. How text is tokenized (Separators).
 * 2. The complexity of patterns extracted (Pattern Length).
 * 3. The memory usage of the application (Pattern Amount Limits).
 */
public final class Constant {

    // Defines the characters that mark the boundaries between words.
    // Used by the ByteReader and SeparatorUtils to split the input stream into processable tokens.
    public static final String SEPARATORS = " \n\t.,;!?";

    // Limits the maximum length of a sub-pattern to be considered for compression.
    // For example, if set to 6, the word "international" will generate patterns up to 6 chars long (e.g., "intern").
    // This prevents the dictionary from growing exponentially with very long, rare patterns.
    public static int MAX_PATTERN_LENGTH = 6;

    // Memory Control: The maximum number of unique patterns allowed per 'Context'.
    // If a Context (e.g., letter 't') accumulates more patterns than this limit,
    // the Eviction Policy (in ContextLeaf) is triggered to remove low-frequency patterns.
    // This is crucial for preventing OutOfMemoryError on large datasets.
    public static int MAX_PATTERN_AMOUNT = 2500;
}
