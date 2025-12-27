package com.pwha.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class for writing individual bits to an OutputStream.
 * <p>
 * Since file systems write data in bytes (8 bits), this class acts as a buffer
 * that accumulates single bits until a full byte is formed, then writes it to the stream.
 * It is essential for writing variable-length Huffman codes.
 */
public class BitWriter implements AutoCloseable {

    // The underlying output stream (e.g., FileOutputStream) where bytes will be written.
    private final OutputStream os;

    // A buffer to hold bits as they are added. It resets once it reaches 8 bits.
    private int currentByte = 0;

    // Tracks the number of bits currently stored in 'currentByte' (0 to 8).
    private int bitCount = 0;

    public BitWriter(OutputStream os) {
        this.os = os;
    }

    /**
     * Writes a single bit (0 or 1) to the buffer.
     *
     * @param bit The bit to write (only the least significant bit is used).
     * @throws IOException If an I/O error occurs when flushing.
     */
    public void writeBit(int bit) throws IOException {
        /*
         * Bitwise Operation:
         * 1. Shift existing bits to the left (<< 1) to make room for the new bit.
         * 2. Perform OR (|) with the new bit to add it at the least significant position.
         * 3. (bit & 1) ensures we only take the last bit of the integer, ignoring others.
         */
        currentByte = (currentByte << 1) | (bit & 1);

        bitCount++;

        // If the buffer is full (8 bits), write it to the stream and reset.
        if(bitCount == 8) {
            flushCurrentByte();
        }
    }

    /**
     * Helper method to write a string of bits (e.g., "101") sequentially.
     *
     * @param bitString A String containing '0's and '1's.
     */
    public void writeBits(String bitString) throws IOException {
        for(int i = 0; i < bitString.length(); i++){
            char c = bitString.charAt(i);
            writeBit(c == '1' ? 1 : 0);
        }
    }

    // Writes the fully formed byte to the output stream and resets the buffer.
    private void flushCurrentByte() throws IOException {
        os.write(currentByte);
        currentByte = 0;
        bitCount = 0;
    }

    /**
     * Flushes any remaining bits in the buffer to the output stream.
     * If the buffer is not empty (has 1-7 bits), it pads the remaining positions
     * with zeros to form a complete byte before writing.
     */
    public void flush() throws IOException {
        if(bitCount > 0) {
            // Shift left to align the valid bits to the most significant positions (Padding).
            currentByte <<= (8-bitCount);
            os.write(currentByte);

            // Reset buffer
            currentByte = 0;
            bitCount = 0;
        }
        os.flush();
    }

    /**
     * Closes the writer.
     * Ensures that any pending bits are flushed to the stream before closing.
     */
    @Override
    public void close() throws IOException {
        flush();
        os.close();
    }
}