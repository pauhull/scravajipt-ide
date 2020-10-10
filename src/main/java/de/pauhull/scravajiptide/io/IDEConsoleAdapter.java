package de.pauhull.scravajiptide.io;

import de.pauhull.scravajipt.io.IOAdapter;
import de.pauhull.scravajiptide.gui.ConsoleWindow;

import javax.swing.*;

public class IDEConsoleAdapter implements IOAdapter {

    private final ConsoleWindow consoleWindow;

    public IDEConsoleAdapter(ConsoleWindow consoleWindow) {

        this.consoleWindow = consoleWindow;
    }

    @Override
    public void output(String s) {

        consoleWindow.getConsoleArea().append(s);

        JScrollBar scrollBar = consoleWindow.getScrollBar().getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

    @Override
    public String input() {

        consoleWindow.text = null;

        while(consoleWindow.text == null);

        return consoleWindow.text;
    }
}
