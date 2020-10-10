package de.pauhull.scravajiptide.gui;

import de.pauhull.scravajipt.program.Program;
import de.pauhull.scravajiptide.io.IDEConsoleAdapter;
import javafx.scene.input.KeyCode;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConsoleWindow extends JFrame {

    public volatile String text;
    private JPanel mainPanel;
    private JTextArea consoleArea;
    private JTextField textField;
    private JButton sendButton;
    private JScrollPane scrollBar;

    public ConsoleWindow(Program program) {

        this.add(mainPanel);
        this.setSize(450, 300);
        this.setLocationRelativeTo(null);
        this.setTitle("Run");
        this.setVisible(true);
        this.requestFocus();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(69, 73, 74));
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 18));

        scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == '\n') {
                    sendLine();
                }
            }
        });

        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendLine();
            }
        });

        program.setIoAdapter(new IDEConsoleAdapter(this));

        new Thread(program::run).start();
    }

    private void sendLine() {

        String line = textField.getText();
        textField.setText("");
        this.text = line;
    }

    public JScrollPane getScrollBar() {
        return scrollBar;
    }

    public JTextArea getConsoleArea() {
        return consoleArea;
    }
}
