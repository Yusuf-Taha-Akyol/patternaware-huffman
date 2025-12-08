package com.pwha.io;

import java.io.IOException;
import java.io.InputStream;

public class BitReader implements AutoCloseable{
    private final InputStream is;
    private int currentByte;
    private int bitCount;

    public BitReader(InputStream is) {
        this.is = is;
        this.currentByte = 0;
        this.bitCount = 0;
    }

    public int readBit() throws IOException {
        if(bitCount == 0) {
            currentByte = is.read();

            if(currentByte == -1) {
                return -1;
            }

            bitCount = 8;
        }

        bitCount--;
        return (currentByte >> bitCount) & 1;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
