package com.pwha.util;

import com.pwha.Main;
import com.pwha.model.node.ContextLeaf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ProcessWord {
    //This class for processing word that coming from ByteReader class.
    //This class important because we are finding pattern from word, and we are processing patterns and first letter of word.
    public static void processWord(byte[] word) throws IOException {

        //Firstly we are processing first letter on word.
        byte firstByte = word[0];
        ContextLeaf firstCharNode;


        if(Main.supperFreqMap.containsKey(firstByte)) {
            //If this first letter is containing in our supper frequency map then we are just increasing frequency of this letter's node.
            Main.supperFreqMap.get(firstByte).increaseFreqByOne();

            //And then we are selecting this existing node as our firstCharNode.
            //We will use this node for calculating sub frequency map of this letter.
            firstCharNode = Main.supperFreqMap.get(firstByte);
        }else{
            //If this first letter is not contains in out supper frequency map then we are creating new object for this letter.
            firstCharNode = new ContextLeaf(firstByte, 1);

            //After creating we are adding this object to in our supper frequency map.
            Main.supperFreqMap.put(firstByte, firstCharNode);
        }

        //This is for finding pattern in word.
        for(int i = 1 ; i < word.length; i++) {
            //We are starting looking from 1 indexes because first letter not important for us because we already proceeded.
            //From head of the word we are looking end of the word.
            //This for loop for start point. i representing start index.
            for(int j = word.length -1 ; j >= i; j--) {
                //Every iteration we are decreasing end point by one.
                //This object important because we want to keep our pattern as byte array.
                //This for loop for end point. j representing end index.
                ByteArrayOutputStream byteArray =  new ByteArrayOutputStream();

                //This for loop for keeping word and finding pattern from start point to end point.
                for(int k = i ; k <= j; k++){
                    //Every letter adding our byte array so that end of the loop we have a pattern as byte array.
                    byteArray.write(word[k]);
                }

                //In here we are adding this pattern to this first letter's sub frequency map.
                byte[] arr =  byteArray.toByteArray();
                if(arr.length == 0){continue;}
                firstCharNode.addToFreqMap(arr);

                //In here we are resting our byte array so that in next iteration we will keep next pattern.
                byteArray.reset();
            }

        }
    }
}
