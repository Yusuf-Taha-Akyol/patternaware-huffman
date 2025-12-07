package com.pwha.model.node;

import com.pwha.model.ByteArrayWrapper;

public class SimpleLeaf extends HNode {
    private ByteArrayWrapper pattern;
    public SimpleLeaf(ByteArrayWrapper pattern, int frequency) {
        super(frequency);
        this.pattern = pattern;
    }

    public ByteArrayWrapper getPattern() {
        return pattern;
    }

    public void setPattern(ByteArrayWrapper pattern) {
        this.pattern = pattern;
    }

    //This method for checking this leaf holding a char or a pattern.
    public boolean isChar(){
        return pattern.length() <= 1;
    }

    //This method for converting byte buffer to String.
    public String convertString(){
        byte[] data = this.pattern.data();

        return new String(data);
    }

    @Override
    public int getPatternLength() {
        return pattern.length();
    }

    @Override
    public boolean isContextLeaf(){
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    //This method important for compare the other nodes.
    //We are comparing nodes in Priority Queue.
    @Override
    public int compareTo(HNode other){
        //Firstly we are comparing node's frequency value.
        //If a node's frequency vale less than the other it means this node will be ahead from the other node.
        //compare method turning an int value.If this value is positive it means our node frequency bigger the other.
        //If this value negative it means our node frequency value smaller than the other.
        //If this value equals the zero it means these two node's frequency value equal.
        int freqCompare = Integer.compare(this.getFrequency(), other.getFrequency());

        //We are checking compare value.If compare value zero like a said before these values equals the other.
        //If compare value different from then 0 we are returning compare value.
        if(freqCompare != 0) {
            return freqCompare;
        }

        //If compare value equals the zero it means our frequency values equals.
        /*
        Because the two nodes have the same frequency,
        Java will randomly place nodes with the same value in the queue.
        However, we still want long patterns to be at the back of the queue.
        Because in the Huffman algorithm, the further you are in the priority queue, the longer your Huffman code will be.
         For byte arrays with the same frequency, we want long patterns to be at the back of the queue, while short patterns are at the front of the queue.
         */
        return Integer.compare(this.getPatternLength(), other.getPatternLength());
    }
}
