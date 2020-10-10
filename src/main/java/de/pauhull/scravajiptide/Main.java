package de.pauhull.scravajiptide;

import com.formdev.flatlaf.FlatDarculaLaf;
import de.pauhull.scravajiptide.gui.MainWindow;

public class Main {

    public static void main(String[] args) {

        FlatDarculaLaf.install();
        new MainWindow();
    }

}
