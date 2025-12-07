package com.pwha.model.node;

import java.io.Serializable;

public abstract class HNode implements Comparable<HNode>, Serializable {
    protected int frequency;
    private HNode parent;
    private HNode left, right;
    public HNode(int frequency) {
        this.frequency = frequency;
    }
    public HNode() {}

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public HNode getParent() {
        return parent;
    }
    public void setParent(HNode parent) {
        this.parent = parent;
    }

    public HNode getLeft() {
        return left;
    }
    public void setLeft(HNode left) {
        this.left = left;
    }

    public HNode getRight() {
        return right;
    }
    public void setRight(HNode right) {
        this.right = right;
    }

    public abstract int getPatternLength();

    public abstract boolean isLeaf();

    public abstract boolean isContextLeaf();
    @Override
    public int compareTo(HNode other){
        return Integer.compare(this.frequency, other.frequency);
    }

}
