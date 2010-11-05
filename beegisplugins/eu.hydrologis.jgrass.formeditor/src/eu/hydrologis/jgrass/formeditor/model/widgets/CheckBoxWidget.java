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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.CHECKBOX_TYPES;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.DEFAULT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.FIELDNAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_CHECK;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_FIELDNAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_TAB;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_TEXT_TYPE;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LOCATION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.SELECTION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.SIZE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TAB_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TEXT_TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDTH_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.XPOS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.YPOS_PROP;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.Constants;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;
/**
 * A checkbox widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CheckBoxWidget extends AWidget {
    public static final String TYPE = "check";

    private static final long serialVersionUID = 1;

    private int defaultValue = 0;
    private int fieldNameValue = 0;

    public CheckBoxWidget() {
        super();
        location = new Point(0, 0);
        size = Constants.DEFAULT_DIMENSION.getCopy();
        initDescriptors();
    }

    /**
     * Initializes the property descriptors array.
     */
    private void initDescriptors() {
        ComboBoxPropertyDescriptor fields = new ComboBoxPropertyDescriptor(FIELDNAME_PROP, LABELS_FIELDNAME,
                FormEditor.getFieldNamesArrays());
        TextPropertyDescriptor x = new TextPropertyDescriptor(XPOS_PROP, "X");
        TextPropertyDescriptor y = new TextPropertyDescriptor(YPOS_PROP, "Y");
        TextPropertyDescriptor w = new TextPropertyDescriptor(WIDTH_PROP, "Width");
        TextPropertyDescriptor h = new TextPropertyDescriptor(HEIGHT_PROP, "Height");
        ComboBoxPropertyDescriptor defaultValue = new ComboBoxPropertyDescriptor(SELECTION_PROP, LABELS_CHECK, CHECKBOX_TYPES);
        defaultValue.setLabelProvider(new CustomLabelProviders.CheckboxLabelProvider());
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(TAB_PROP, LABELS_TAB);
        descriptors = new IPropertyDescriptor[]{fields, x, y, w, h, defaultValue, tabValue};

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

    public Object getPropertyValue( Object propertyId ) {
        if (SELECTION_PROP.equals(propertyId)) {
            return getDefaultValue();
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            return getFieldnameValue();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (SELECTION_PROP.equals(propertyId)) {
            setDefaultValue((Integer) value);
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            setFieldnameValue((Integer) value);
        } else {
            super.setPropertyValue(propertyId, value);
        }
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue( int defaultValue ) {
        this.defaultValue = defaultValue;
        firePropertyChange(SELECTION_PROP, null, defaultValue);
    }

    public int getFieldnameValue() {
        return fieldNameValue;
    }

    public void setFieldnameValue( int fieldNameValue ) {
        this.fieldNameValue = fieldNameValue;
        firePropertyChange(FIELDNAME_PROP, null, fieldNameValue);
    }
}
