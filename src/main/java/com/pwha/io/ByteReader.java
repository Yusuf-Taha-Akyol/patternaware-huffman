package com.pwha.io;

import com.pwha.service.FrequencyService;
import com.pwha.util.SeparatorUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class ByteReader implements AutoCloseable {
    private final FrequencyService frequencyService;
    private final InputStream inputStream;

    public ByteReader(FrequencyService frequencyService, InputStream inputStream) {
        this.frequencyService = frequencyService;
        this.inputStream = inputStream;
    }

    //Head of the program.
    public void collectWords(long totalSize, Consumer<Double> onProgress) throws IOException{
        ByteArrayOutputStream wordBuffer = new ByteArrayOutputStream();
        int byteRead;
        long bytesReadSoFar = 0;

        try{

            while((byteRead = inputStream.read()) != -1){
                bytesReadSoFar ++;
                if(onProgress != null && bytesReadSoFar % 10240 ==0 ){
                    double percent = (double) bytesReadSoFar / totalSize * 100;
                    onProgress.accept(percent);
                }
                //This variable holding byte value that we were read.
                byte currentByte =  (byte) byteRead;

                if(SeparatorUtils.isSeparator(currentByte)){
                    if(wordBuffer.size() > 0){
                        byte[] word = wordBuffer.toByteArray();

                        frequencyService.processWord(word);
                        frequencyService.addSeparatorToContext(word[0], currentByte);
                        wordBuffer.reset();
                    }
                    byte[] separatorsByte= {currentByte};
                    frequencyService.processWord(separatorsByte);

                }else {
                    wordBuffer.write(currentByte);
                }
            }
        }finally{
            if(wordBuffer.size() > 0){
                frequencyService.processWord(wordBuffer.toByteArray());
                wordBuffer.reset();
            }
            wordBuffer.close();
        }
    }

    public void collectWords() throws IOException{
        collectWords(1, null);
    }

    @Override
    public void close() throws Exception {

    }
}
