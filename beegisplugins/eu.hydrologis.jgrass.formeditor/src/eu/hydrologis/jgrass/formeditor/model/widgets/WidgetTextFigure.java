package eu.hydrologis.jgrass.formeditor.model.widgets;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.Constants;

public class WidgetTextFigure extends ImageFigure {
    private final AWidget widget;

    public WidgetTextFigure( AWidget widget, Image image ) {
        super(image, PositionConstants.NORTH_WEST);
        this.widget = widget;

        setBorder(new LineBorder(ColorConstants.black, 1));
        setForegroundColor(ColorConstants.black);
    }

    protected void paintFigure( Graphics graphics ) {
        super.paintFigure(graphics);

        Rectangle area = getClientArea();
        int x = area.x + Constants.FIELDNAME_OFFSET_X;
        int y = area.y + Constants.FIELDNAME_OFFSET_Y;
        String text = widget.getText();
        if (text.length() == 0) {
            text = widget.getName();
        }

        graphics.drawText(text, x, y);
    }
}
