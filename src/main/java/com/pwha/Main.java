package com.pwha;

import com.pwha.core.HuffmanStructure;
import com.pwha.engine.Decoder;
import com.pwha.engine.Encoder;
import com.pwha.io.ByteReader;
import com.pwha.model.node.HNode;
import com.pwha.service.FrequencyService;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String inputFile = "deneme.txt";
        String outputFile = "encoded_file.pwha";
        String decodedFile = "decoded_file.txt";

        FrequencyService frequencyService = new FrequencyService();

        System.out.println("1.Stage : File Analyzing...");
        try(FileInputStream fis = new FileInputStream(inputFile)){
            ByteReader reader = new ByteReader(frequencyService,fis);
            reader.collectWords();
        }

        if(frequencyService.getFrequencyMap().isEmpty()){
            throw new RuntimeException("Frequency Map is empty");
        }

        System.out.println("2.Stage : Huffman Structure...");
        HNode root = HuffmanStructure.buildSuperTree(HuffmanStructure.setQueue(frequencyService.getFrequencyMap()));
        HuffmanStructure.buildDictionary(root,"",frequencyService.getFrequencyMap());

        System.out.println("3.Stage : Compressing...");
        Encoder encoder = new Encoder(root,frequencyService.getFrequencyMap());
        encoder.compress(inputFile,outputFile);

        Decoder decoder = new Decoder();
        decoder.decompress(outputFile,decodedFile);
    }
}