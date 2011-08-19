/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.formeditor.model.widgets;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.ColorCache;
import eu.hydrologis.jgrass.formeditor.utils.Constants;

/**
 * The {@link ImageFigure} that represents all widgets.
 * 
 * <p>Contains an image and some text.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class WidgetTextFigure extends ImageFigure {
    private final AWidget widget;

    public WidgetTextFigure( AWidget widget, Image image ) {
        super(image, PositionConstants.NORTH_WEST);
        this.widget = widget;

        String text = widget.getTab();
        Color color = ColorCache.getInstance().getColor(text);
        setBorder(new LineBorder(color, 2));
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
