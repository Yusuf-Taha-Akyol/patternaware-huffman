package com.pwha.engine;

import com.pwha.io.BitWriter;
import com.pwha.model.ByteArrayWrapper;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.util.SeparatorUtils;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class Encoder {
    private final HNode root;
    private final Map<Byte, ContextLeaf> dictionary;

    public Encoder(HNode root, Map<Byte, ContextLeaf> dictionary) {
        this.root = root;
        this.dictionary = dictionary;
    }
    public void compress(String inputFile, String outputFile, long totalSize, Consumer<Double> onProgress) throws IOException {
        System.out.println("Compressing and File Writing");

        try(FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            BitWriter bitWriter = new BitWriter(bos)) {

            writeHeader(fos);
            encodeContent(inputFile, bitWriter, totalSize, onProgress);
        }

        System.out.println("Compressing complete... " + outputFile);
    }

    public void compress(String inputFile, String outputFile) throws IOException {
        compress(inputFile, outputFile, 1, null);
    }
    private void writeHeader(OutputStream outputFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputFile);
        oos.writeObject(this.dictionary);
        oos.flush();
    }
    private void encodeContent(String inputFile, BitWriter bitWriter, long totalSize, Consumer<Double> onProgress) throws IOException {

        try(FileInputStream fis = new FileInputStream(inputFile);
            BufferedInputStream bis = new BufferedInputStream(fis)){

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int data;
            long bytesReadSoFar = 0;

            while((data = bis.read()) != -1) {
                bytesReadSoFar++;

                if(onProgress != null && bytesReadSoFar % 10240 == 0) {
                    double percent = (double) bytesReadSoFar / totalSize * 100;
                    onProgress.accept(percent);
                }

                byte byteValue = (byte) data;
                if (SeparatorUtils.isSeparator(byteValue)) {
                    if(bos.size() > 0){
                        encodeWord(bos.toByteArray(), bitWriter);
                    }

                    if(bos.size() > 0){
                        byte contextByte = bos.toByteArray()[0];
                        ContextLeaf contextLeaf = this.dictionary.get(contextByte);

                        String localSepCode = null;
                        if(contextLeaf != null) {
                            localSepCode = contextLeaf.getSubCode(new ByteArrayWrapper(new byte[]{byteValue}));
                        }

                        if(localSepCode != null){
                            bitWriter.writeBits(localSepCode);
                        }else{
                            encodeGlobalSeparator(byteValue, bitWriter);
                        }
                    } else {
                        encodeGlobalSeparator(byteValue, bitWriter);
                    }
                    bos.reset();
                }else{
                    bos.write(byteValue);
                }
            }

            if(bos.size() > 0){
                encodeWord(bos.toByteArray(), bitWriter);
            }

        }

    }

    private void encodeWord(byte[] word, BitWriter bitWriter) throws IOException {
        if(word.length == 0){return;}
        byte contextByte = word[0];

        ContextLeaf contextNode = this.dictionary.get(contextByte);

        if(contextNode == null){
            throw new IOException("Dictionary match failed for byte : " + contextByte);
        }

        bitWriter.writeBits(contextNode.getCode());

        if(word.length > 1){
            byte[] remain =  Arrays.copyOfRange(word, 1, word.length);
            processGreedyMatch(remain,  bitWriter, contextNode);
        }
    }

    private void encodeGlobalSeparator(byte separator, BitWriter bitWriter) throws IOException {
        ContextLeaf globalSepNode = this.dictionary.get(separator);
        if(globalSepNode != null){
            bitWriter.writeBits(globalSepNode.getCode());
        } else {
            System.out.println("Char couldn't found in Map : " + (char) separator);
        }
    }

    private void processGreedyMatch(byte[] byteValue, BitWriter bitWriter, ContextLeaf contextNode) throws IOException {
        int start = 0;
        while (start < byteValue.length) {
            boolean found = false;

            for(int end = byteValue.length; end > start; end--){
                byte[] sub = Arrays.copyOfRange(byteValue, start, end);
                ByteArrayWrapper key = new ByteArrayWrapper(sub);

                String code = contextNode.getSubCode(key);

                if(code != null){
                    bitWriter.writeBits(code);
                    start = end;
                    found = true;
                    break;
                }
            }

            if(!found){
                start++;
            }
        }
    }
}
