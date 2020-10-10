package de.pauhull.scravajiptide.ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class IDETextPane extends JTextPane {

    private String tooltipText;
    private Point tooltipLocation;

    private final List<Map.Entry<String, int[]>> errors = new ArrayList<>();

    public void addError(int start, int end, String message) {
        errors.add(new AbstractMap.SimpleEntry<>(message, new int[]{start, end}));
    }

    public void clearErrors() {
        errors.clear();
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {

        if(e.getKeyChar() != '\t') {
            super.processKeyEvent(e);
            if(e.getID() == KeyEvent.KEY_RELEASED && tooltipText != null) updateTooltip(getCaretPosition());
            return;
        }

        if(e.getID() == KeyEvent.KEY_TYPED) {

            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_SPACE);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);

        int offset = viewToModel(e.getPoint());
        updateTooltip(offset);
    }

    private void updateTooltip(int offset) {

        tooltipText = null;

        for (Map.Entry<String, int[]> entry : errors) {

            String message = entry.getKey();
            int start = entry.getValue()[0];
            int end = entry.getValue()[1];

            if (offset >= start && offset <= end) {
                try {
                    tooltipLocation = modelToView(end).getLocation();
                    tooltipLocation.y += 5;
                    tooltipLocation.x += 15;
                } catch (BadLocationException badLocationException) {
                    badLocationException.printStackTrace();
                }
                tooltipText = message;
            }
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
            graphics.fillRoundRect(tooltipLocation.x - padding, tooltipLocation.y - padding, rect.width + padding * 2, rect.height + padding * 2, round, round);

            graphics.setPaint(new Color(0xFA6565));
            graphics.drawString(tooltipText, tooltipLocation.x, tooltipLocation.y + graphics.getFontMetrics().getAscent());
            repaint();
        }
    }
}