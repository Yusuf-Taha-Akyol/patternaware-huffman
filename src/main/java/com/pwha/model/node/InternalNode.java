package com.pwha.model.node;

public class InternalNode extends HNode {
    public InternalNode(HNode leftNode, HNode rightNode) {
        super(leftNode.getFrequency() + rightNode.getFrequency());
    }

    @Override
    public boolean isContextLeaf(){
        return false;
    }

    @Override
    public int getPatternLength() {return 0;}

    @Override
    public boolean isLeaf() {
        return false;
    }

}
