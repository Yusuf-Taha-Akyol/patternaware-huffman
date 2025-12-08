package com.pwha.engine;

import com.pwha.core.HuffmanStructure;
import com.pwha.io.BitReader;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.InternalNode;
import com.pwha.model.node.SimpleLeaf;
import com.pwha.util.SeparatorUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class Decoder {
    private HashMap<Byte, ContextLeaf> globalContextMap;
    private HNode globalTreeRoot;

    public void decompress(String compressedFile, String outputFile) throws IOException , ClassNotFoundException {
        System.out.println("Decompressing " + compressedFile + " to " + outputFile);

        FileInputStream fis = new FileInputStream(compressedFile);

        ObjectInputStream ois = new ObjectInputStream(fis);

        this.globalContextMap = (HashMap<Byte, ContextLeaf>) ois.readObject();

        System.out.println("Dictionary is downloaded. Huffman Tree is rebuilding...");

        rebuildAllTrees();

        BitReader bitReader = new BitReader(fis);
        FileOutputStream fos = new FileOutputStream(outputFile);

        decodeContent(bitReader, fos);

        fos.close();
        bitReader.close();
        System.out.println("Decompressing complete...");
    }

    private void rebuildAllTrees() {
        for(ContextLeaf contextNode : globalContextMap.values()) {
            contextNode.setSubQueue();
            HuffmanStructure.buildSubTree(contextNode);
        }

        var globalQueue = HuffmanStructure.setQueue(globalContextMap);
        this.globalTreeRoot = HuffmanStructure.buildSuperTree(globalQueue);
    }

    private void decodeContent(BitReader bitReader, FileOutputStream fos) throws IOException {
        ContextLeaf currentContext = null;
        while(true) {
            HNode currentNode;
            if(currentContext == null || currentContext.getSubTreeRoot() == null) {
                currentNode = globalTreeRoot;
            }else {
                currentNode = currentContext.getSubTreeRoot();
            }

            while(!currentNode.isLeaf()) {
                int bit = bitReader.readBit();

                if(bit == -1) {
                    return;
                }

                InternalNode internal = (InternalNode) currentNode;
                currentNode = (bit == 0) ? internal.getLeft() : internal.getRight();

                if(currentNode == null) {return;}
            }

            if(currentNode instanceof ContextLeaf) {
                ContextLeaf leaf = (ContextLeaf) currentNode;
                byte data = leaf.getData();
                fos.write(data);

                if(SeparatorUtils.isSeparator(data)){
                    currentContext = null;
                } else{
                    currentContext  = leaf;
                }
            }else if(currentNode instanceof SimpleLeaf) {
                SimpleLeaf leaf = (SimpleLeaf) currentNode;
                byte[] data = leaf.getPattern().data();
                fos.write(data);

                if(data.length == 1 && SeparatorUtils.isSeparator(data[0])){
                    currentContext = null;
                }else{

                }
            }
        }
    }
}