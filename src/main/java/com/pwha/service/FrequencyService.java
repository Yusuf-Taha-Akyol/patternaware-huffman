package com.pwha.service;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;
import com.pwha.util.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class FrequencyService {
    //This class for processing word that coming from ByteReader class.
    //This class important because we are finding pattern from word, and we are processing patterns and first letter of word.

    private final HashMap<Byte,ContextLeaf> frequencyMap;

    public FrequencyService() {
        this.frequencyMap = new HashMap<>();
    }
    public void processWord(byte[] word) {
        if(word == null || word.length == 0) return;
        //Firstly we are processing first letter on word.
        byte contextSymbol = word[0];
        ContextLeaf contextNode;


        if(frequencyMap.containsKey(contextSymbol)) {
            //If this first letter is containing in our supper frequency map then we are just increasing frequency of this letter's node.
            contextNode = frequencyMap.get(contextSymbol);
            //And then we are selecting this existing node as our contextNode.
            //We will use this node for calculating sub frequency map of this letter.
            contextNode.increaseFreqByOne();
        }else{
            //If this first letter is not contains in out supper frequency map then we are creating new object for this letter.
            contextNode = new ContextLeaf(contextSymbol, 1);

            //After creating we are adding this object to in our supper frequency map.
            frequencyMap.put(contextSymbol, contextNode);
        }

        extractAndAddPatterns(contextNode, word);
    }

    private void extractAndAddPatterns(ContextLeaf contextNode, byte[] word){
        for(int start = 1; start <word.length; start++){

            int maxEnd = Math.min(word.length - 1, start + Constant.MAX_PATTERN_LENGTH -1);

            for(int end = maxEnd; end >= start; end--){
                byte[] pattern = Arrays.copyOfRange(word, start, end + 1);
                contextNode.addToFreqMap(pattern);
            }
        }
    }

    public void addSeparatorToContext(byte contextSymbol, byte separator){
        if(frequencyMap.containsKey(contextSymbol)){
            ContextLeaf contextNode = frequencyMap.get(contextSymbol);

            contextNode.addToFreqMap(new byte[]{separator});
        }
    }
    public HashMap<Byte,ContextLeaf> getFrequencyMap() {
        return frequencyMap;
    }
}
