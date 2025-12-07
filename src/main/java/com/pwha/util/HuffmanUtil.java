package com.pwha.util;

import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.SimpleLeaf;

public class HuffmanUtil {
    public static void printUpperTreeCodes(HNode root, StringBuilder sb){
        if(root == null)return;

        if(root.isLeaf()){
            ContextLeaf contextLeaf = (ContextLeaf) root;
            System.out.println((char) contextLeaf.getData() + " Karakterinin üst Ağaç Değeri : " + sb.toString());
            printSubTreeCodes(contextLeaf.getSubTreeRoot(), new StringBuilder());
            System.out.println("---------------------------");
        }

        if(root.getLeft() != null){
            printUpperTreeCodes(root.getLeft(), sb.append(0));
            sb.deleteCharAt(sb.length()-1);
        }

        if(root.getRight() != null){
            printUpperTreeCodes(root.getRight(), sb.append(1));
            sb.deleteCharAt(sb.length()-1);
        }
    }

    public static void printSubTreeCodes(HNode root, StringBuilder sb) {
        if(root == null)return;

        if(root.isLeaf()){
            SimpleLeaf simpleLeaf = (SimpleLeaf) root;
            System.out.println(simpleLeaf.convertString() + " Deseni Alt Ağaç Değeri : " + sb.toString() );
        }

        if(root.getLeft() != null){
            printSubTreeCodes(root.getLeft(), sb.append(0));
            sb.deleteCharAt(sb.length()-1);
        }

        if(root.getRight() != null){
            printSubTreeCodes(root.getRight(), sb.append(1));
            sb.deleteCharAt(sb.length()-1);
        }
    }
}
