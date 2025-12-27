package com.pwha.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for reading data bit-by-bit from an InputStream.
 * <p>
 * Standard Java streams read bytes (8 bits), but Huffman codes are variable-length sequences of bits
 * (e.g., "101" or "0"). This class acts as a bridge, buffering bytes and serving them one bit at a time.
 */
public class BitReader implements AutoCloseable{

    // The underlying input stream (e.g., from a file).
    private final InputStream is;

    // Buffer to hold the current byte being processed.
    private int currentByte;

    // Counter tracking how many bits are left in the current byte (0 to 8).
    private int bitCount;

    public BitReader(InputStream is) {
        this.is = is;
        this.currentByte = 0;
        this.bitCount = 0;
    }

    /**
     * Reads the next single bit from the stream.
     * * @return 0 or 1, or -1 if the End of Stream (EOF) is reached.
     * @throws IOException If an I/O error occurs.
     */
    public int readBit() throws IOException {
        // If we have used all bits in the current byte (buffer is empty), read the next byte.
        if(bitCount == 0) {
            currentByte = is.read();

            // Check for End of File
            if(currentByte == -1) {
                return -1;
            }

            // Reset counter to 8 since a new byte has 8 bits.
            bitCount = 8;
        }

        // Decrement the counter to point to the next bit position.
        bitCount--;

        /*
         * Bitwise Operation Logic:
         * 1. Shift the current byte to the right by 'bitCount'.
         * This moves the target bit to the least significant position (index 0).
         * 2. Perform bitwise AND with 1 (binary ...0001).
         * This isolates the target bit, ignoring all other bits.
         */
        return (currentByte >> bitCount) & 1;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
