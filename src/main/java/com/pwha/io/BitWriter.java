package com.pwha.io;

import java.io.IOException;
import java.io.OutputStream;

public class BitWriter implements AutoCloseable {
    private final OutputStream os;
    private int currentByte = 0;
    private int bitCount = 0;

    public BitWriter(OutputStream os) {
        this.os = os;
    }

    public void writeBit(int bit) throws IOException {
        currentByte = (currentByte << 1) | (bit & 1);

        bitCount++;

        if(bitCount == 8) {
            flushCurrentByte();
        }
    }

    public void writeBits(String bitString) throws IOException {
        for(int i = 0; i < bitString.length(); i++){
            char c = bitString.charAt(i);
            writeBit(c == '1' ? 1 : 0);
        }
    }

    private void flushCurrentByte() throws IOException {
        os.write(currentByte);
        currentByte = 0;
        bitCount = 0;
    }

    public void flush() throws IOException {
        if(bitCount > 0) {
            currentByte <<= (8-bitCount);
            os.write(currentByte);
            currentByte = 0;
            bitCount = 0;
        }
        os.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        os.close();
    }
}
