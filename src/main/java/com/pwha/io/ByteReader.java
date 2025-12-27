package com.pwha.io;

import com.pwha.service.FrequencyService;
import com.pwha.util.SeparatorUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Reads raw bytes from the input stream and segments them into processable "words".
 * <p>
 * This class serves as the entry point for the Analysis Phase. It reads the file byte-by-byte,
 * identifies word boundaries using {@link SeparatorUtils}, and feeds the extracted tokens
 * to the {@link FrequencyService} for pattern mining.
 */
public class ByteReader implements AutoCloseable {

    // Service to handle frequency counting and pattern extraction.
    private final FrequencyService frequencyService;

    // The source stream to read from (e.g., FileInputStream).
    private final InputStream inputStream;

    public ByteReader(FrequencyService frequencyService, InputStream inputStream) {
        this.frequencyService = frequencyService;
        this.inputStream = inputStream;
    }

    /**
     * The main loop for analyzing the file.
     * Reads the stream, splits content into words/separators, and triggers processing.
     *
     * @param totalSize  Total size of the file (for progress calculation).
     * @param onProgress Callback to update the UI progress bar.
     * @throws IOException If a read error occurs.
     */
    // Head of the program.
    public void collectWords(long totalSize, Consumer<Double> onProgress) throws IOException{
        // Buffer to accumulate bytes forming a word.
        ByteArrayOutputStream wordBuffer = new ByteArrayOutputStream();
        int byteRead;
        long bytesReadSoFar = 0;

        try{
            // Read byte by byte until End of Stream (-1) is reached.
            while((byteRead = inputStream.read()) != -1){
                bytesReadSoFar ++;

                // Calculate and report progress every 10KB to avoid UI flooding.
                if(onProgress != null && bytesReadSoFar % 10240 ==0 ){
                    double percent = (double) bytesReadSoFar / totalSize * 100;
                    onProgress.accept(percent);
                }

                // This variable holding byte value that we read.
                byte currentByte =  (byte) byteRead;

                // Check if the current byte is a separator (e.g., space, newline, punctuation).
                if(SeparatorUtils.isSeparator(currentByte)){

                    // If we have accumulated a word in the buffer, process it now.
                    if(wordBuffer.size() > 0){
                        byte[] word = wordBuffer.toByteArray();

                        // 1. Process the word itself (extract patterns).
                        frequencyService.processWord(word);

                        // 2. Associate this separator with the Context of the word (first letter).
                        // This helps model "which punctuation usually follows this word/context".
                        frequencyService.addSeparatorToContext(word[0], currentByte);

                        // Clear the buffer for the next word.
                        wordBuffer.reset();
                    }

                    // Process the separator itself as a distinct token/pattern.
                    byte[] separatorsByte= {currentByte};
                    frequencyService.processWord(separatorsByte);

                }else {
                    // It's a regular character; add it to the buffer.
                    wordBuffer.write(currentByte);
                }
            }
        }finally{
            // Process any remaining bytes in the buffer after the loop ends (EOF).
            if(wordBuffer.size() > 0){
                frequencyService.processWord(wordBuffer.toByteArray());
                wordBuffer.reset();
            }
            wordBuffer.close();
        }
    }

    // Overloaded method for simple execution without progress tracking.
    public void collectWords() throws IOException{
        collectWords(1, null);
    }

    @Override
    public void close() throws Exception {
        // Implementation left empty; stream management is handled by the caller or try-with-resources blocks.
    }
}
