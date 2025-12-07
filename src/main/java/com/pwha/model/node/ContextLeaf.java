package com.pwha.model.node;

import com.pwha.model.ByteArrayWrapper;
import com.pwha.util.Constant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ContextLeaf extends HNode implements Serializable {
    private final byte data;
    private HNode subTreeRoot;
    private final HashMap<ByteArrayWrapper, Integer> freqMap;
    private final HashMap<ByteArrayWrapper, String> subDictionary;
    private PriorityQueue<SimpleLeaf> priorityQueue;
    private String code;
    private int kValue;

    public ContextLeaf(byte data, int frequency) {
        super(frequency);
        this.data = data;
        this.freqMap = new HashMap<>();
        this.subDictionary = new HashMap<>();
        this.priorityQueue = new PriorityQueue<>();
    }

    public byte getData(){
        return this.data;
    }

    public HNode getSubTreeRoot(){
        return subTreeRoot;
    }

    public void setSubTreeRoot(HNode subTreeRoot) {
        this.subTreeRoot = subTreeRoot;
    }

    public HashMap<ByteArrayWrapper, Integer> getSubFreqMap(){
        return this.freqMap;
    }

    public void addToFreqMap(byte[] key) {
        //There will be added Space-Save algorithm
        if(key.length > Constant.MAX_PATTERN_LENGTH){
            return;
        }
        ByteArrayWrapper byteArrayWrapper = new ByteArrayWrapper(key);

        if(byteArrayWrapper.length() == 1){
            freqMap.put(byteArrayWrapper, freqMap.getOrDefault(byteArrayWrapper, 0)+1);
        }
        if(freqMap.containsKey(byteArrayWrapper)){
            freqMap.put(byteArrayWrapper, freqMap.get(byteArrayWrapper) + 1);
            return;
        }

        if(kValue < Constant.MAX_PATTERN_AMOUNT){
            freqMap.put(byteArrayWrapper, freqMap.getOrDefault(byteArrayWrapper,1));
            kValue++;
            return;
        }

        evictSafeAndAdd(byteArrayWrapper);
    }

    public void evictSafeAndAdd(ByteArrayWrapper byteArrayWrapper){
        ByteArrayWrapper victim = null;
        int minFreq = Integer.MAX_VALUE;

        for(Map.Entry<ByteArrayWrapper, Integer> entry : freqMap.entrySet()){
            ByteArrayWrapper key = entry.getKey();

            if(key.data().length == 1){
                continue;
            }

            if(entry.getValue() < minFreq){
                minFreq = entry.getValue();
                victim = key;
            }
        }

        if(victim != null){
            freqMap.remove(victim);
            freqMap.put(byteArrayWrapper, minFreq + 1);
        } else{
            if(byteArrayWrapper.data().length == 1){
                freqMap.put(byteArrayWrapper, minFreq + 1);
            }
        }


    }

    public PriorityQueue<SimpleLeaf> getSubQueue(){
        return this.priorityQueue;
    }

    public void setSubQueue(){
        this.priorityQueue = new PriorityQueue<>();
        for(ByteArrayWrapper key : freqMap.keySet()){
            this.priorityQueue.add(new SimpleLeaf(key, freqMap.get(key)));
        }

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    //Alt ağaca göre dictionary oluşturmak için.
    public void generateCode() {
        subDictionary.clear();

        if(subTreeRoot != null){
            traverseAndBuildDictionary(subTreeRoot, "");
        }
    }

    private void traverseAndBuildDictionary(HNode node, String currentCode){
        if(node == null) return;

        if(node instanceof SimpleLeaf simpleLeaf){

            this.subDictionary.put(simpleLeaf.getPattern(), currentCode);
        }

        if(node instanceof InternalNode internalNode){

            traverseAndBuildDictionary(internalNode.getLeft(), currentCode + "0");
            traverseAndBuildDictionary(internalNode.getRight(), currentCode + "1");
        }
    }

    public String getSubCode(ByteArrayWrapper key){
        return subDictionary.get(key);
    }

    public void increaseFreqByOne() {
        setFrequency(getFrequency() + 1);
    }

    @Override
    public boolean isContextLeaf(){
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getPatternLength() {return 1;}
}
