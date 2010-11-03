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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_H;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_W;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_X;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_Y;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_NAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_ORIENTATION;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_TAB;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.NAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ORIENTATION_TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TAB_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TEXT_TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDTH_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.XPOS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.YPOS_PROP;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.Constants;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;
/**
 * A label widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class SeparatorWidget extends AWidget {
    public static final String TYPE = "separator";

    private static final long serialVersionUID = 1;

    private int typeValue = 0;

    public SeparatorWidget() {
        super();
        location = new Point(0, 0);
        size = Constants.DEFAULT_DIMENSION.getCopy();
        initDescriptors();
    }

    /**
     * Initializes the property descriptors array.
     */
    private void initDescriptors() {
        TextPropertyDescriptor x = new TextPropertyDescriptor(XPOS_PROP, LABELS_LAYOUT_X);
        TextPropertyDescriptor y = new TextPropertyDescriptor(YPOS_PROP, LABELS_LAYOUT_Y);
        TextPropertyDescriptor w = new TextPropertyDescriptor(WIDTH_PROP, LABELS_LAYOUT_W);
        TextPropertyDescriptor h = new TextPropertyDescriptor(HEIGHT_PROP, LABELS_LAYOUT_H);
        TextPropertyDescriptor nameValue = new TextPropertyDescriptor(NAME_PROP, LABELS_NAME);
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(TAB_PROP, LABELS_TAB);
        ComboBoxPropertyDescriptor orientationTypes = new ComboBoxPropertyDescriptor(ORIENTATION_TYPE_PROP, LABELS_ORIENTATION,
                Constants.ORIENTATION_TYPES);
        orientationTypes.setLabelProvider(new CustomLabelProviders.OrientationTypesLabelProvider());
        descriptors = new IPropertyDescriptor[]{x, y, w, h, orientationTypes, nameValue, tabValue};

        addIntegerPropertyValidator(x);
        addIntegerPropertyValidator(y);
        addIntegerPropertyValidator(w);
        addIntegerPropertyValidator(h);
        addIntegerPropertyValidator(tabValue);
    }

    public Image getIcon() {
        return ImageCache.getInstance().getImage(ImageCache.LABEL_ICON_16);
    }

    public String toString() {
        return "Separator " + hashCode();
    }

    public int getTypeValue() {
        return typeValue;
    }

    public void setTypeValue( int typeValue ) {
        this.typeValue = typeValue;
        firePropertyChange(TEXT_TYPE_PROP, null, typeValue);
    }

    public Object getPropertyValue( Object propertyId ) {
        if (ORIENTATION_TYPE_PROP.equals(propertyId)) {
            return getTypeValue();
        } else if (NAME_PROP.equals(propertyId)) {
            return getName();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (ORIENTATION_TYPE_PROP.equals(propertyId)) {
            setTypeValue((Integer) value);
        } else if (NAME_PROP.equals(propertyId)) {
            String defValue = (String) value;
            setName(defValue);
        } else {
            super.setPropertyValue(propertyId, value);
        }
    }

}
