package de.pauhull.scravajiptide;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;

public class Main {

    public static void main(String[] args) throws Exception {

        FlatDarculaLaf.install();
        new MainGUI();
    }

}
