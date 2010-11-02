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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.FIELDNAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LOCATION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.SELECTION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.SIZE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDTH_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.XPOS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.YPOS_PROP;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;
/**
 * A checkbox widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CheckBoxWidget extends AWidget {
    public static final String TYPE = "check";

    private static final long serialVersionUID = 1;

    private String selected = "false";

    public CheckBoxWidget() {
        super();
        location = new Point(0, 0);
        size = new Dimension(341, 41);
        initDescriptors();
    }

    /**
     * Initializes the property descriptors array.
     */
    private void initDescriptors() {
        TextPropertyDescriptor x = new TextPropertyDescriptor(XPOS_PROP, "X");
        TextPropertyDescriptor y = new TextPropertyDescriptor(YPOS_PROP, "Y");
        TextPropertyDescriptor w = new TextPropertyDescriptor(WIDTH_PROP, "Width");
        TextPropertyDescriptor h = new TextPropertyDescriptor(HEIGHT_PROP, "Height");
        TextPropertyDescriptor fieldName = new TextPropertyDescriptor(FIELDNAME_PROP, "Field Name");
        TextPropertyDescriptor selected = new TextPropertyDescriptor(SELECTION_PROP, "Default selection");
        descriptors = new IPropertyDescriptor[]{fieldName, x, y, w, h, selected};

        addIntegerPropertyValidator(x);
        addIntegerPropertyValidator(y);
        addIntegerPropertyValidator(w);
        addIntegerPropertyValidator(h);
    }

    public Image getIcon() {
        return ImageCache.getInstance().getImage(ImageCache.CHECK_ICON_16);
    }

    public String toString() {
        return "Check " + hashCode();
    }

    public void setSize( Dimension newSize ) {
        // fixed size widget
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected( String selected ) {
        this.selected = selected;
        firePropertyChange(SELECTION_PROP, null, selected);
    }

    public Object getPropertyValue( Object propertyId ) {
        if (SELECTION_PROP.equals(propertyId)) {
            return getSelected();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (SELECTION_PROP.equals(propertyId)) {
            setSelected((String) value);
        } else {
            super.setPropertyValue(propertyId, value);
        }
    }

    public String toDumpString() {
        String tmpName = getFieldname().replaceAll("\\s+", "_");
        Dimension tmpSize = getSize();
        Point tmpLocation = getLocation();
        String tmpSel = getSelected();

        StringBuilder sB = new StringBuilder();
        sB.append(tmpName).append(".").append(TYPE_PROP).append("=");
        sB.append(TYPE).append("\n");
        sB.append(tmpName).append(".").append(FIELDNAME_PROP).append("=");
        sB.append(tmpName).append("\n");
        sB.append(tmpName).append(".").append(SIZE_PROP).append("=");
        sB.append(tmpSize.width).append(",").append(tmpSize.height).append("\n");
        sB.append(tmpName).append(".").append(LOCATION_PROP).append("=");
        sB.append(tmpLocation.x).append(",").append(tmpLocation.y).append("\n");
        sB.append(tmpName).append(".").append(SELECTION_PROP).append("=");
        sB.append(tmpSel).append("\n");

        return sB.toString();
    }
}
