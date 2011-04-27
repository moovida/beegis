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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.Constants;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.*;
/**
 * A label widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class LabelWidget extends AWidget {
    public static final String TYPE = "label";

    private static final long serialVersionUID = 1;

    private String textValue = "";

    public LabelWidget() {
        super();
        location = new Point(0, 0);
        size = Constants.DEFAULT_DIMENSION.getCopy();
        initDescriptors();
    }

    /**
     * Initializes the property descriptors array.
     */
    private void initDescriptors() {
        TextPropertyDescriptor type = new TextPropertyDescriptor(ID_TYPE_PROP, WIDGET_TYPE);
        TextPropertyDescriptor x = new TextPropertyDescriptor(ID_XPOS_PROP, WIDGET_LAYOUT_X);
        TextPropertyDescriptor y = new TextPropertyDescriptor(ID_YPOS_PROP, WIDGET_LAYOUT_Y);
        TextPropertyDescriptor w = new TextPropertyDescriptor(ID_WIDTH_PROP, WIDGET_LAYOUT_W);
        TextPropertyDescriptor h = new TextPropertyDescriptor(ID_HEIGHT_PROP, WIDGET_LAYOUT_H);
        TextPropertyDescriptor textValue = new TextPropertyDescriptor(ID_TEXT_PROP, WIDGET_TEXT);
        TextPropertyDescriptor nameValue = new TextPropertyDescriptor(ID_NAME_PROP, WIDGET_NAME);
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(ID_TAB_PROP, WIDGET_TAB);
        descriptors = new IPropertyDescriptor[]{type, x, y, w, h, textValue, nameValue, tabValue};

        addIntegerPropertyValidator(x);
        addIntegerPropertyValidator(y);
        addIntegerPropertyValidator(w);
        addIntegerPropertyValidator(h);
    }

    public Image getIcon() {
        return ImageCache.getInstance().getImage(ImageCache.LABEL_ICON_16);
    }

    public String toString() {
        return "Label " + hashCode();
    }

    public String getText() {
        return textValue;
    }

    public void setText( String textValue ) {
        if (textValue == null) {
            throw new IllegalArgumentException();
        }
        this.textValue = textValue;
        firePropertyChange(ID_TEXT_PROP, null, textValue);
    }

    public Object getPropertyValue( Object propertyId ) {
        if (ID_TEXT_PROP.equals(propertyId)) {
            return getText();
        } else if (ID_NAME_PROP.equals(propertyId)) {
            return getName();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (ID_TEXT_PROP.equals(propertyId)) {
            String defValue = (String) value;
            setText(defValue);
        }else if (ID_NAME_PROP.equals(propertyId)) {
            String defValue = (String) value;
            setName(defValue);
        } else {
            super.setPropertyValue(propertyId, value);
        }
    }

    public String toDumpString() {
        String tmpName = getName().replaceAll("\\s+", "_");
        Dimension tmpSize = getSize();
        Point tmpLocation = getLocation();
        String tmpTextValue = getText();

        StringBuilder sB = new StringBuilder();
        sB.append(tmpName).append(".").append(ID_TYPE_PROP).append("=");
        sB.append(TYPE).append("\n");
        sB.append(tmpName).append(".").append(ID_FIELDNAME_PROP).append("=");
        sB.append(tmpName).append("\n");
        sB.append(tmpName).append(".").append(ID_SIZE_PROP).append("=");
        sB.append(tmpSize.width).append(",").append(tmpSize.height).append("\n");
        sB.append(tmpName).append(".").append(ID_LOCATION_PROP).append("=");
        sB.append(tmpLocation.x).append(",").append(tmpLocation.y).append("\n");
        sB.append(tmpName).append(".").append(ID_TEXT_PROP).append("=");
        sB.append(tmpTextValue).append("\n");

        return sB.toString();
    }

    public String getWidgetType() {
        return TYPE;
    }
}
