package com.pwha.util;

import java.util.HashSet;
import java.util.Set;

public class SeparatorUtils {
    private static final Set<Byte> SEPERATOR_SET = new HashSet<Byte>();

    static {
        for(byte b : Constant.SEPARATORS.getBytes()){
            SEPERATOR_SET.add(b);
        }
    }

    private SeparatorUtils() {}

    public static boolean isSeparator(byte b){
        return SEPERATOR_SET.contains(b);
    }

    public static Set<Byte> getSeparators(){
        return SEPERATOR_SET;
    }
}
