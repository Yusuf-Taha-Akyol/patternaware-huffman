package com.pwha;

import com.pwha.core.HuffmanStructure;
import com.pwha.engine.Encoder;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.ByteArrayWrapper;
import com.pwha.io.ByteReader;
import com.pwha.util.HuffmanUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static HashMap<Byte, ContextLeaf> supperFreqMap = new HashMap<>();
    public static HashMap<Byte, ContextLeaf> dictionary = new HashMap<>();
    public static HNode upperTreeRoot;

    public static void main(String[] args) throws IOException {

        Scanner input = new Scanner(System.in);
        String inputFile = "deneme.txt";
        String outputFile = "output.pwha";

        Encoder encoder = new Encoder();
        encoder.compress(inputFile,outputFile);

    }
}

