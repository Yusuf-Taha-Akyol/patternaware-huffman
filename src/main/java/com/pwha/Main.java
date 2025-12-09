package com.pwha;

import com.pwha.core.HuffmanStructure;
import com.pwha.engine.Decoder;
import com.pwha.engine.Encoder;
import com.pwha.gui.App;
import com.pwha.io.ByteReader;
import com.pwha.model.node.HNode;
import com.pwha.service.FrequencyService;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        SwingUtilities.invokeLater(() -> {
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e){
                e.printStackTrace();
            }

            new App().setVisible(true);
        });
    }
}