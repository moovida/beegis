package eu.hydrologis.jgrass.formeditor.model.widgets;

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
        
        
        LineBorder border = new LineBorder(2);
        setBorder(border);
    }

    protected void paintFigure( Graphics graphics ) {
        super.paintFigure(graphics);
        Rectangle area = getClientArea();
        int x = area.x + Constants.FIELDNAME_OFFSET_X;
        int y = area.y + 5;

        String fieldname = widget.getFieldname();
        graphics.drawText(fieldname, x, y);
    }
}
