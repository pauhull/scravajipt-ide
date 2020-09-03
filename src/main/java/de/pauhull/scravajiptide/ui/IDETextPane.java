package de.pauhull.scravajiptide.ui;

import de.pauhull.scravajipt.program.Variable;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class IDETextPane extends JTextPane {

    private String tooltipText;
    private Point mouseLocation;

    private List<Map.Entry<String, int[]>> errors = new ArrayList<>();

    public void addError(int start, int end, String message) {
        errors.add(new AbstractMap.SimpleEntry<>(message, new int[]{start, end}));
    }

    public void clearErrors() {
        errors.clear();
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);

        int offset = viewToModel(e.getPoint());

        boolean foundError = false;

        for (Map.Entry<String, int[]> entry : errors) {

            String message = entry.getKey();
            int start = entry.getValue()[0];
            int end = entry.getValue()[1];

            if (offset >= start && offset <= end) {
                try {
                    mouseLocation = modelToView(end).getLocation();
                    mouseLocation.y += 5;
                    mouseLocation.x += 15;
                } catch (BadLocationException badLocationException) {
                    badLocationException.printStackTrace();
                }
                tooltipText = message;
                foundError = true;
            }
        }

        if(!foundError) {
            tooltipText = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(tooltipText != null) {
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setFont(new Font("Consolas", Font.PLAIN, 12));

            int padding = 5;
            int round = 10;
            Rectangle rect = graphics.getFontMetrics().getStringBounds(tooltipText, graphics).getBounds();
            graphics.setPaint(new Color(0x343434));
            graphics.fillRoundRect(mouseLocation.x - padding, mouseLocation.y - padding, rect.width + padding * 2, rect.height + padding * 2, round, round);

            graphics.setPaint(new Color(0xFA6565));
            graphics.drawString(tooltipText, mouseLocation.x, mouseLocation.y + graphics.getFontMetrics().getAscent());
            repaint();
        }
    }
}