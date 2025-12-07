package com.pwha.core;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.InternalNode;
import com.pwha.model.node.SimpleLeaf;

import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanStructure {
    public static PriorityQueue<ContextLeaf> setQueue(HashMap<Byte, ContextLeaf> freqMap) {
        PriorityQueue<ContextLeaf> priorityQueue = new PriorityQueue<>();
        for(ContextLeaf contextLeaf : freqMap.values()){
            contextLeaf.setSubQueue();
            priorityQueue.add(contextLeaf);
        }

        return priorityQueue;
    }

    public static HNode buildSuperTree(PriorityQueue<ContextLeaf> Queue) {
        PriorityQueue<HNode> pqUpper = new PriorityQueue<>();
        pqUpper.addAll(Queue);

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

    public static void buildSubTree(ContextLeaf contextLeaf) {
        PriorityQueue<HNode> pqSub = new PriorityQueue<>();
        contextLeaf.setSubQueue();
        pqSub.addAll(contextLeaf.getSubQueue());

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

        contextLeaf.setSubTreeRoot(pqSub.poll());
    }

    public static void buildDictionary(HNode node, String currentCode) {
        if(node == null){return;}

        if(node instanceof ContextLeaf contextLeaf){
            contextLeaf.setCode(currentCode);
            Main.dictionary.put(contextLeaf.getData(), contextLeaf);
            contextLeaf.generateCode();
        }

        if(node instanceof InternalNode internalNode){

            buildDictionary(internalNode.getLeft(),currentCode + 0);
            buildDictionary(internalNode.getRight(), currentCode + 1);
        }

    }

    public static ContextLeaf getContextLeaf(byte key) {
        return Main.dictionary.get(key);
    }

    public static void printQueue(PriorityQueue<ContextLeaf> queue) {
        while (!queue.isEmpty()) {
            ContextLeaf contextLeaf = queue.poll();
            System.out.println((char) contextLeaf.getData() + " Karakterinin üst ağaç Değeri : " + contextLeaf.getFrequency());

            PriorityQueue<SimpleLeaf> subQueue = contextLeaf.getSubQueue();
            while (!subQueue.isEmpty()) {
                SimpleLeaf simpleLeaf = subQueue.poll();
                System.out.println(simpleLeaf.convertString() + ":" +  simpleLeaf.getFrequency());
            }
        }
    }
}
