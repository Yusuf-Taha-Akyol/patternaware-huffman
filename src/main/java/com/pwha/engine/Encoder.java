package com.pwha.engine;

import com.pwha.Main;
import com.pwha.core.HuffmanStructure;
import com.pwha.io.BitWriter;
import com.pwha.io.ByteReader;
import com.pwha.model.ByteArrayWrapper;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.util.Constant;
import com.pwha.util.HuffmanUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Encoder {
    public void compress(String inputFile, String outputFile) throws IOException {
        Scanner sc = new Scanner(System.in);

        //First Step Analyzing File. It means we are taking datas from our files.
        System.out.println("Compressing " + inputFile + " to " + outputFile);
        System.out.println("1. Stage : File Analyzing...");
        analyzeFile(inputFile);

        //Second step is building Huffman Tree and Dictionary.
        System.out.println("2. Stage : Huffman Structure...");
        buildTreesAndCodes();
        HuffmanUtil.printUpperTreeCodes(Main.upperTreeRoot, new StringBuilder());

        System.out.println("3. Stage : Compressing and File Writing...");

        //We are writing Dictionary first. We will use dictionary when we want to decode.
        FileOutputStream fos = new FileOutputStream(outputFile);
        writeHeader(fos);


        BitWriter bitWriter = new BitWriter(fos);
        encodeContent(inputFile, bitWriter);

        bitWriter.close();

        System.out.println("Compressing complete... " + outputFile);


        /*
        System.out.print("Kodu dönecek char değerini yazınız :");
        String value = sc.nextLine();
        byte[] charValue = value.getBytes(StandardCharsets.UTF_8);
        ContextLeaf cl = HuffmanStructure.getContextLeaf(charValue[0]);

        System.out.println(cl.getCode());

        String pattern = sc.nextLine();

        ByteArrayWrapper patternBytes = new ByteArrayWrapper(pattern.getBytes(StandardCharsets.UTF_8));

        System.out.println(cl.getSubCode(patternBytes));

         */

    }

    private void analyzeFile(String inputFile)  throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile)){
            ByteReader reader = new ByteReader();
            reader.collectWords(fis);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void buildTreesAndCodes(){
        PriorityQueue<ContextLeaf> queue = HuffmanStructure.setQueue(Main.supperFreqMap);
        HNode root = HuffmanStructure.buildSuperTree(queue);
        HuffmanStructure.buildDictionary(root,"");

        Main.upperTreeRoot = root;
    }

    private void writeHeader(OutputStream outputFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputFile);
        oos.writeObject(Main.dictionary);
        oos.flush();
    }

    private void encodeContent(String inputFile, BitWriter bitWriter) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int data;
        while((data = fis.read()) != -1) {
            byte b = (byte) data;
            if (getSeparators().contains(b)) {
                if(bos.size() > 0){
                    encodeWord(bos.toByteArray(), bitWriter);
                }

                ContextLeaf contextLeaf = HuffmanStructure.getContextLeaf(bos.toByteArray()[0]);

                String localSepCode = contextLeaf.getSubCode(new ByteArrayWrapper(new byte[]{b}));

                if(localSepCode != null){
                    bitWriter.writeBits(localSepCode);
                }else{
                    ContextLeaf globalSep = HuffmanStructure.getContextLeaf(b);
                    bitWriter.writeBits(globalSep.getCode());
                }

                bos.reset();
            }else{
                bos.write(b);
            }
        }

        if(bos.size() > 0){
            encodeWord(bos.toByteArray(), bitWriter);
        }

        fis.close();
    }

    private void encodeWord(byte[] word, BitWriter bitWriter) throws IOException {
        if(word.length == 0){return;}

        byte contextByte = word[0];
        ContextLeaf contextLeaf = HuffmanStructure.getContextLeaf(contextByte);
        bitWriter.writeBits(contextLeaf.getCode());

        if(word.length > 1){
            byte[] remain =  Arrays.copyOfRange(word, 1, word.length);
            processGreedyMatch(remain,  bitWriter, contextLeaf);
        }
    }

    private void processGreedyMatch(byte[] data, BitWriter bitWriter, ContextLeaf contextLeaf) throws IOException {
        int start = 0;
        while (start < data.length) {
            boolean found = false;

            for(int end = data.length; end > start; end--){
                byte[] sub = Arrays.copyOfRange(data, start, end);
                ByteArrayWrapper key = new ByteArrayWrapper(sub);

                String code = contextLeaf.getSubCode(key);

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
    private static Set<Byte> getSeparators(){
        Set<Byte> separators = new HashSet<>();
        for(byte b : Constant.SEPARATORS.getBytes()){
            separators.add(b);
        }
        return separators;
    }
}
