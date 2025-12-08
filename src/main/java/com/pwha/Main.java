package com.pwha;

import com.pwha.engine.Decoder;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import java.io.IOException;
import java.util.HashMap;

public class Main {
    public static HashMap<Byte, ContextLeaf> supperFreqMap = new HashMap<>();
    public static HashMap<Byte, ContextLeaf> dictionary = new HashMap<>();
    public static HNode upperTreeRoot;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String inputFile = "input.txt";
        String outputFile = "encoded_file.pwha";
        String decodedFile = "decoded_file.txt";
        //Encoder encoder = new Encoder();
        //encoder.compress(inputFile,outputFile);
        Decoder decoder = new Decoder();
        decoder.decompress(outputFile,decodedFile);
    }
}