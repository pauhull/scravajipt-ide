package de.pauhull.scravajiptide.gui;
import de.pauhull.scravajipt.compiler.Compiler;

import de.pauhull.scravajipt.compiler.CompilerException;
import de.pauhull.scravajipt.program.Program;
import de.pauhull.scravajipt.syntax.InstructionSyntax;
import de.pauhull.scravajiptide.ui.ErrorUnderline;
import de.pauhull.scravajiptide.ui.IDETextPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MainWindow extends JFrame {

    private static final String[] stringsToHighlight = {"+", "-", "*", "/", "==", "<=", ">=", "!=", "<", ">", "&&", "||", "true", "false", "%"};
    private final Style defaultStyle, instructionStyle, commentStyle, variableStyle, stringStyle, highlightStyle, errorStyle;

    private JPanel mainPanel;
    private JButton newButton;
    private JTextPane textPane;
    private JScrollPane scrollBar;
    private JButton openButton;
    private JButton saveButton;
    private JButton exportButton;
    private JButton runButton;

    public MainWindow() {

        this.add(mainPanel);
        this.setSize(700, 900);
        this.setLocationRelativeTo(null);
        this.setTitle("ScravaJipt IDE");
        this.setVisible(true);
        this.requestFocus();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(textPane.getDocument().getLength() != 0) {
                    askForSave();
                }
            }
        });

        UndoManager undoManager = new UndoManager();
        textPane.getStyledDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                System.out.println("test");
                undoManager.addEdit(e.getEdit());
            }
        });

        this.mainPanel.registerKeyboardAction(e -> undoManager.undo(), KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        this.mainPanel.registerKeyboardAction(e -> openFile(), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.mainPanel.registerKeyboardAction(e -> saveProjectFile(), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        newButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if(textPane.getDocument().getLength() != 0) {
                        askForSave();
                    }

                    textPane.getDocument().remove(0, textPane.getDocument().getLength());
                    ((IDETextPane) textPane).clearErrors();
                } catch (BadLocationException badLocationException) {
                    badLocationException.printStackTrace();
                }
            }
        });

        openButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openFile();
            }
        });

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveProjectFile();
            }
        });

        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveProgramFile();
            }
        });

        runButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                try {
                    Compiler compiler = new Compiler();
                    String text = textPane.getDocument().getText(0, textPane.getDocument().getLength());

                    Program program;

                    try {
                        program = compiler.compile(text);
                    } catch (CompilerException ex) {

                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Couldn't compile program because there was an error in line " + (ex.line + 1) + ": " + ex.message);
                        return;
                    }

                    new ConsoleWindow(program);
                } catch (BadLocationException badLocationException) {
                    badLocationException.printStackTrace();
                }
            }
        });

        textPane.setFont(new Font("Consolas", Font.PLAIN, 18));
        textPane.setEditorKit(new ErrorUnderline.UnderlineEditorKit());
        scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        StyleContext styleContext = new StyleContext();

        defaultStyle = styleContext.addStyle("default", null);
        defaultStyle.addAttribute(StyleConstants.Foreground, new Color(231, 231, 231));
        defaultStyle.addAttribute(StyleConstants.Background, new Color(0x0000000, true));
        defaultStyle.addAttribute(StyleConstants.Bold, false);
        defaultStyle.addAttribute(StyleConstants.Italic, false);
        defaultStyle.addAttribute("error-underline", false);

        instructionStyle = styleContext.addStyle("instruction", null);
        instructionStyle.addAttribute(StyleConstants.Foreground, new Color(252, 3, 177));
        instructionStyle.addAttribute(StyleConstants.Bold, true);

        commentStyle = styleContext.addStyle("comment", null);
        commentStyle.addAttribute(StyleConstants.Foreground, new Color(125, 125, 125));
        commentStyle.addAttribute(StyleConstants.Italic, true);

        variableStyle = styleContext.addStyle("variable", null);
        variableStyle.addAttribute(StyleConstants.Italic, true);

        stringStyle = styleContext.addStyle("string", null);
        stringStyle.addAttribute(StyleConstants.Foreground, new Color(0x65935A));

        highlightStyle = styleContext.addStyle("highlight", null);
        highlightStyle.addAttribute(StyleConstants.Foreground, new Color(255, 174, 0));

        errorStyle = styleContext.addStyle("error", null);
        errorStyle.addAttribute("error-underline", true);

        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateHighlighting();
            }
        });
    }

    private void askForSave() {

        if(JOptionPane.showConfirmDialog(null, "Would you like to save your work?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            saveProjectFile();
        }
    }

    private void openFile() {

        if(textPane.getDocument().getLength() != 0) {
            askForSave();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("ScravaJipt Project File", "sj"));

        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();

            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {

                StringBuilder builder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                textPane.getDocument().remove(0, textPane.getDocument().getLength());
                textPane.getDocument().insertString(0, builder.toString(), null);
                updateHighlighting();

            } catch (IOException | BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProgramFile() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("ScravaJipt Program File", "prog"));

        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();

            if(!file.getName().toLowerCase().endsWith(".prog")) {
                file = new File(file.getParentFile(), file.getName() + ".prog");
            }

            String compiled;
            Compiler compiler = new Compiler();
            String code = null;
            try {
                code = textPane.getDocument().getText(0, textPane.getDocument().getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            try {
                compiled = compiler.compile(code).toJson().toString();
            } catch (CompilerException ex) {

                JOptionPane.showMessageDialog(this,
                        "Couldn't compile program because there was an error in line " + (ex.line + 1) + ": " + ex.message);
                return;
            }

            try(PrintWriter writer = new PrintWriter(new FileWriter(file))) {

                writer.print(compiled);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProjectFile() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("ScravaJipt Project File", "sj"));

        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();

            if(!file.getName().toLowerCase().endsWith(".sj")) {
                file = new File(file.getParentFile(), file.getName() + ".sj");
            }

            try(PrintWriter writer = new PrintWriter(new FileWriter(file))) {

                writer.print(textPane.getDocument().getText(0, textPane.getDocument().getLength()));

            } catch (IOException | BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void createUIComponents() {
        textPane = new IDETextPane();
    }

    private void updateHighlighting() {

        StyledDocument document = textPane.getStyledDocument();
        Compiler compiler = new Compiler();

        ((IDETextPane) textPane).clearErrors();

        String documentText;
        try {
            documentText = document.getText(0, document.getLength());
        } catch (BadLocationException badLocationException) {
            badLocationException.printStackTrace();
            return;
        }

        document.setCharacterAttributes(0, documentText.length(), defaultStyle, false);

        int offset = 0;
        for(String line : documentText.split("\n")) {

            int lineOffset = 0;
            for(char c : line.toCharArray()) {
                if(c == ' ' || c == '\t') {
                    lineOffset++;
                } else {
                    break;
                }
            }
            line = line.substring(lineOffset);

            if(line.startsWith("#") || line.startsWith("//")) {
                document.setCharacterAttributes(offset, lineOffset + line.length(), commentStyle, false);
                offset += lineOffset + line.length() + 1;
                continue;
            }

            for(InstructionSyntax syntax : InstructionSyntax.syntaxLookup) {

                if(line.toLowerCase().startsWith(syntax.name + " ") || line.toLowerCase().equals(syntax.name)) {

                    for(String highlight : stringsToHighlight) {

                        int index = -1;
                        do {
                            index = line.indexOf(highlight, index+1);

                            if(index != -1) {
                                document.setCharacterAttributes(offset + lineOffset + index, highlight.length(), highlightStyle, false);
                            }
                        } while(index != -1);
                    }

                    document.setCharacterAttributes(offset + lineOffset, syntax.name.length(), instructionStyle, false);

                    if(line.length() > syntax.name.length()) {

                        String args = line.substring(syntax.name.length());

                        boolean statementMode = false;
                        int string = -1;
                        int beginIndex = -1;
                        int argIndex = 0;
                        for(int charIndex = 0; charIndex < args.length(); charIndex++) {

                            char c = args.charAt(charIndex);

                            if(argIndex >= syntax.parameters.length) {
                                break;
                            }

                            if(syntax.parameters[argIndex] == InstructionSyntax.Parameter.STATEMENT) {
                                statementMode = true;
                            }

                            if(statementMode) {

                                if(c == '"') {
                                    if(string == -1) {
                                        string = charIndex;
                                    } else {
                                        document.setCharacterAttributes(offset + lineOffset + syntax.name.length() + string, charIndex - string + 1, stringStyle, false);
                                        string = -1;
                                    }
                                }

                            } else {
                                if (beginIndex == -1 && c != ' ') {
                                    beginIndex = charIndex;
                                }

                                if (c == ' ' || charIndex == args.length() - 1) {

                                    if (charIndex > 0 && c == ' ' && args.charAt(charIndex - 1) == ' ') {
                                        continue;
                                    }

                                    if (charIndex > 0 && beginIndex != -1) {

                                        int length;
                                        if (charIndex == args.length() - 1 && c != ' ') {
                                            length = args.length() - beginIndex;
                                        } else {
                                            length = charIndex - beginIndex;
                                        }

                                        document.setCharacterAttributes(offset + lineOffset + syntax.name.length() + beginIndex, length, variableStyle, false);

                                        argIndex++;
                                        beginIndex = -1;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            offset += lineOffset + line.length() + 1;
        }

        String[] lines = documentText.split("\n");
        int off = 0;
        for (String line : lines) {

            int lineOffset = 0;

            for(char c : line.toCharArray()) {
                if(c == ' ') {
                    lineOffset++;
                } else {
                    break;
                }
            }

            line = line.substring(lineOffset);

            try {
                compiler.compile(line);
            } catch (CompilerException ex) {

                if(!ex.message.equals("Expected container end")
                        && !ex.message.equals("No container to end")) {
                    document.setCharacterAttributes(off + lineOffset, line.length(), errorStyle, false);
                    ((IDETextPane) textPane).addError(off + lineOffset, off + lineOffset + line.length(), ex.message);
                }
            }

            off += line.length() + lineOffset + 1;
        }
    }
}
