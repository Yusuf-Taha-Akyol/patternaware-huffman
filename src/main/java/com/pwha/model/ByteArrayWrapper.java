package com.pwha.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A wrapper record for byte arrays to ensure correct behavior in HashMaps and Sets.
 * <p>
 * Problem: Native {@code byte[]} arrays in Java use reference equality for {@code hashCode()} and {@code equals()}.
 * This means two different array objects with the exact same bytes are considered "different" keys in a Map.
 * <p>
 * Solution: This record overrides {@code equals} and {@code hashCode} to compare the
 * ACTUAL CONTENT of the arrays using {@code Arrays.equals}.
 */
public record ByteArrayWrapper(byte[] data) implements Serializable {

    /**
     * Returns the length of the wrapped byte array.
     * Helper method to provide cleaner access than {@code data().length}.
     */
    public int length() {
        return data.length;
    }

    /**
     * Checks equality based on the actual byte content, not the memory address.
     * Essential for identifying if a pattern (e.g., "the") has already been seen.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArrayWrapper that = (ByteArrayWrapper) o;
        return Arrays.equals(data, that.data);
    }

    /**
     * Generates a hash code based on the byte content.
     * Ensures that arrays with the same bytes fall into the same bucket in a HashMap.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Returns a string representation of the array content.
     * Primarily used for debugging purposes.
     */
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
