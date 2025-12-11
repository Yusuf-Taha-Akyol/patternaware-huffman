package com.pwha.core;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.InternalNode;
import com.pwha.util.CustomPriorityQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanStructure {
    public static CustomPriorityQueue<ContextLeaf> setQueue(Map<Byte, ContextLeaf> freqMap) {
        CustomPriorityQueue<ContextLeaf> pq = new CustomPriorityQueue<>();
        for(ContextLeaf contextNode : freqMap.values()){
            pq.add(contextNode);
        }

        return pq;
    }

    public static HNode buildSuperTree(CustomPriorityQueue<ContextLeaf> pq) {
        CustomPriorityQueue<HNode> pqUpper = new CustomPriorityQueue<>();
        pqUpper.addAll(pq);

        while (pqUpper.size() > 1) {
            HNode left =  pqUpper.poll();
            HNode right = pqUpper.poll();
            if(left.isLeaf()) {
                buildSubTree((ContextLeaf) left);
            }
            if(right.isLeaf()) {
                buildSubTree((ContextLeaf) right);
            }

            InternalNode parentNode = new InternalNode(left, right);

            parentNode.setLeft(left);
            parentNode.setRight(right);
            left.setParent(parentNode);
            right.setParent(parentNode);

            pqUpper.add(parentNode);
        }

        return pqUpper.poll();
    }

    public static void buildSubTree(ContextLeaf contextNode) {
        CustomPriorityQueue<HNode> pqSub = new CustomPriorityQueue<>();
        contextNode.setSubQueue();
        pqSub.addAll(contextNode.getSubQueue());

        contextNode.clearSubQueue();

        while (pqSub.size() > 1) {
            HNode left = pqSub.poll();
            HNode right = pqSub.poll();

            InternalNode parentNode = new InternalNode(left, right);

            parentNode.setLeft(left);
            parentNode.setRight(right);
            left.setParent(parentNode);
            right.setParent(parentNode);

            pqSub.add(parentNode);
        }

        contextNode.setSubTreeRoot(pqSub.poll());
    }

    public static void buildDictionary(HNode node, String currentCode, Map<Byte, ContextLeaf> dictionary) {
        if(node == null){return;}

        if(node instanceof ContextLeaf contextNode){
            contextNode.setCode(currentCode);
            dictionary.put(contextNode.getData(), contextNode);
            contextNode.generateCode();
        }

        if(node instanceof InternalNode internalNode){
            buildDictionary(internalNode.getLeft(),currentCode + 0,dictionary);
            buildDictionary(internalNode.getRight(), currentCode + 1,dictionary);
        }

    }
}
