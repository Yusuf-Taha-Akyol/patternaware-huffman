package com.pwha.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to identify Word Separators.
 * Defines which characters (e.g., Space, Newline, Punctuation) delimit words.
 * Used by ByteReader to split the stream into processable tokens.
 */
public class SeparatorUtils {

    // Hash set for O(1) lookup performance
    private static final Set<Byte> SEPERATOR_SET = new HashSet<Byte>();

    // Static initialization block to populate the set from Constants
    static {
        for(byte b : Constant.SEPARATORS.getBytes()){
            SEPERATOR_SET.add(b);
        }
    }

    private SeparatorUtils() {} // Prevent instantiation

    /**
     * Checks if a byte corresponds to a separator character.
     */
    public static boolean isSeparator(byte b){
        return SEPERATOR_SET.contains(b);
    }

    public static Set<Byte> getSeparators(){
        return SEPERATOR_SET;
    }
}
