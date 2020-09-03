package de.pauhull.scravajiptide.ui;

import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.*;
import java.awt.*;

public class ErrorUnderline {

    public static class UnderlineEditorKit extends StyledEditorKit {

        @Override
        public ViewFactory getViewFactory() {
            return new UnderlineTextPaneUI();
        }
    }

    public static class UnderlineTextPaneUI extends BasicTextPaneUI {

        @Override
        public View create(Element elem) {

            View result;
            String kind = elem.getName();
            if(kind != null){
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        result = new ErrorLabelView(elem);
                        break;
                    case AbstractDocument.ParagraphElementName:
                        result = new ParagraphView(elem);
                        break;
                    case AbstractDocument.SectionElementName:
                        result = new BoxView(elem, View.Y_AXIS);
                        break;
                    case StyleConstants.ComponentElementName:
                        result = new ComponentView(elem);
                        break;
                    case StyleConstants.IconElementName:
                        result = new IconView(elem);
                        break;
                    default:
                        result = new LabelView(elem);
                        break;
                }
            } else {
                result = super.create(elem);
            }

            return result;
        }
    }

    public static class ErrorLabelView extends LabelView {

        public ErrorLabelView(Element elem) {
            super(elem);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            super.paint(g, a);

            Object o = getElement().getAttributes().getAttribute("error-underline");
            if(o == null || !(boolean) o) return;

            Color color = new Color(0xD23636);

            int y = a.getBounds().y + (int) getGlyphPainter().getAscent(this) + 5;
            int x1 = a.getBounds().x;
            int x2 = a.getBounds().width + x1;

            int[] pointsX = new int[x2-x1];
            int[] pointsY = new int[pointsX.length];

            for(int x = 0; x < pointsX.length; x++) {
                pointsX[x] = x1 + x;
                pointsY[x] = y + ((Math.floorDiv(x, 2)) % 2 == 0 ? 1 : 0);
            }

            g.setColor(color);
            g.drawPolyline(pointsX, pointsY, pointsX.length);
        }
    }
}
