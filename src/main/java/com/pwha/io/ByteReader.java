package com.pwha.io;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;
import com.pwha.util.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static com.pwha.util.ProcessWord.processWord;

public class ByteReader implements AutoCloseable {
    //This class beginning of this program.
    //We are reading our file with this class.
    //We are reading as byte stream. When we saw a separators we are sending our word to ProcessWord class.

    //Head of the program.
    public void collectWords(InputStream input) throws IOException{
        //We will hold words in a word buffer.
        //So that this object necessary.
        ByteArrayOutputStream wordBuffer = new ByteArrayOutputStream();
        int byteRead;

        try{
            //In while, we are continue until this file over.
            //If our byteRead value that representing byte value actually we read. If this value equals -1 is that mean our file is over.
            while((byteRead = input.read()) != -1){
                //This variable holding byte value that we were read.
                byte currentByte =  (byte) byteRead;

                //If our current byte is one of the separators byte value it means our word is finished, and we will start next word.
                if(getSeparators().contains(currentByte)){
                    //In this if statement we are checking our word buffer is holding any value.
                    //If word buffer size bigger than 0 it means this word buffer holding a word as byte array or at least a char byte value.
                    if(wordBuffer.size() > 0){
                        //Here we are sending this word buffer to process word class to process this word.
                        processWord(wordBuffer.toByteArray());

                        //We are resting this buffer so that we can keep next word in this buffer.
                    }

                    //After if statement we proceed our word.But we have to process current byte that actually a separators.
                    byte[] separatorsByte= {currentByte};
                    if(Main.supperFreqMap.containsKey(wordBuffer.toByteArray()[0])){
                        ContextLeaf contextLeaf = Main.supperFreqMap.get(wordBuffer.toByteArray()[0]);
                        contextLeaf.addToFreqMap(separatorsByte);
                    }

                    wordBuffer.reset();
                }else {
                    //If current byte is not a separators it means it is a char, and we are adding this char byte value to our word buffer.
                    wordBuffer.write(currentByte);
                }
            }
        }finally{
            //We have to add word process statement at the finally block.
            //Because when we finish a file, there may not be a separator value at the end of the file.
            //With this final block if there is an any value in our word buffer we don't forget this value, and we are processing.
            if(wordBuffer.size() > 0){
                processWord(wordBuffer.toByteArray());
                wordBuffer.reset();
            }
            wordBuffer.close();
        }
    }

    //This method not necessary but maybe in the future users want to change separators.
    //So that I thought maybe a method for getting separators byte value as a Set maybe will usefully.
    private static Set<Byte> getSeparators(){
        Set<Byte> separators = new HashSet<>();
        for(byte b : Constant.SEPARATORS.getBytes()){
            separators.add(b);
        }
        return separators;
    }


    @Override
    public void close() throws Exception {

    }
}
