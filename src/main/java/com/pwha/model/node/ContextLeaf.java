package com.pwha.model.node;

import com.pwha.model.ByteArrayWrapper;
import com.pwha.util.Constant;
import com.pwha.util.CustomPriorityQueue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ContextLeaf extends HNode implements Serializable {
    private final byte data;
    private final HashMap<ByteArrayWrapper, Integer> freqMap;
    private transient HNode subTreeRoot;
    private transient final HashMap<ByteArrayWrapper, String> subDictionary;
    private transient CustomPriorityQueue<SimpleLeaf> priorityQueue;
    private transient String code;
    private int kValue;

    public ContextLeaf(byte data, int frequency) {
        super(frequency);
        this.data = data;
        this.freqMap = new HashMap<>();
        this.subDictionary = new HashMap<>();
        this.priorityQueue = new CustomPriorityQueue<SimpleLeaf>();
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

    public void addToFreqMap(byte[] pattern) {
        //There will be added Space-Save algorithm
        if(pattern.length > Constant.MAX_PATTERN_LENGTH){
            return;
        }

        ByteArrayWrapper byteArrayWrapper = new ByteArrayWrapper(pattern);

        Integer count = freqMap.get(byteArrayWrapper);
        if(count != null){
            freqMap.put(byteArrayWrapper, count + 1);
            return;
        }

        if(byteArrayWrapper.length() == 1){
            freqMap.put(byteArrayWrapper, 1);
            return;
        }

        if(freqMap.size() < Constant.MAX_PATTERN_AMOUNT){
            freqMap.put(byteArrayWrapper, 1);
            return;
        }

        evictSafeAndAdd(byteArrayWrapper);
    }

    public void evictSafeAndAdd(ByteArrayWrapper byteArrayWrapper){
        ByteArrayWrapper victim = null;
        int minFreq = Integer.MAX_VALUE;


        int sampleSize = 100;
        int checked = 0;

        for(Map.Entry<ByteArrayWrapper, Integer> entry : freqMap.entrySet()){

            if(entry.getKey().data().length == 1) continue;

            int val = entry.getValue();

            if(val == 1){
                victim = entry.getKey();
                minFreq = val;
                break;
            }

            if(val < minFreq){
                minFreq = val;
                victim = entry.getKey();
            }

            checked++;
            if(checked >= sampleSize) break;
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

    public CustomPriorityQueue<SimpleLeaf> getSubQueue(){
        return this.priorityQueue;
    }

    public void setSubQueue(){
        this.priorityQueue = new CustomPriorityQueue<>();
        for(ByteArrayWrapper key : freqMap.keySet()){
            this.priorityQueue.add(new SimpleLeaf(key, freqMap.get(key)));
        }

    }

    public void clearSubQueue(){
        this.priorityQueue = null;
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

        if(node instanceof SimpleLeaf simpleNode){

            this.subDictionary.put(simpleNode.getPattern(), currentCode);
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

    public int getPatternCount() {
        return freqMap.size();
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
