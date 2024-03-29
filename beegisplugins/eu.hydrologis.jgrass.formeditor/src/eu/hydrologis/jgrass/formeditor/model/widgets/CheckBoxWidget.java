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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.*;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_FIELDNAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_CHECK;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_FIELDNAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_H;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_W;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_X;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_Y;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_TAB;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_SELECTION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TAB_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_WIDTH_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_XPOS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_YPOS_PROP;

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
    public static final String TYPE = "check"; //$NON-NLS-1$

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
        TextPropertyDescriptor type = new TextPropertyDescriptor(ID_TYPE_PROP, WIDGET_TYPE);
        ComboBoxPropertyDescriptor fields = new ComboBoxPropertyDescriptor(ID_FIELDNAME_PROP, WIDGET_FIELDNAME,
                FormEditor.getFieldNamesArrays());
        TextPropertyDescriptor x = new TextPropertyDescriptor(ID_XPOS_PROP, WIDGET_LAYOUT_X);
        TextPropertyDescriptor y = new TextPropertyDescriptor(ID_YPOS_PROP, WIDGET_LAYOUT_Y);
        TextPropertyDescriptor w = new TextPropertyDescriptor(ID_WIDTH_PROP, WIDGET_LAYOUT_W);
        TextPropertyDescriptor h = new TextPropertyDescriptor(ID_HEIGHT_PROP, WIDGET_LAYOUT_H);
        ComboBoxPropertyDescriptor defaultValue = new ComboBoxPropertyDescriptor(ID_SELECTION_PROP, WIDGET_CHECK, CHECKBOX_TYPES);
        defaultValue.setLabelProvider(new CustomLabelProviders.CheckboxLabelProvider());
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(ID_TAB_PROP, WIDGET_TAB);
        descriptors = new IPropertyDescriptor[]{type, fields, x, y, w, h, defaultValue, tabValue};

        addIntegerPropertyValidator(x);
        addIntegerPropertyValidator(y);
        addIntegerPropertyValidator(w);
        addIntegerPropertyValidator(h);
    }

    public Image getIcon() {
        return ImageCache.getInstance().getImage(ImageCache.CHECK_ICON_16);
    }

    public String toString() {
        return "Check " + hashCode(); //$NON-NLS-1$
    }

    public Object getPropertyValue( Object propertyId ) {
        if (ID_SELECTION_PROP.equals(propertyId)) {
            return getDefaultValue();
        } else if (ID_FIELDNAME_PROP.equals(propertyId)) {
            return getFieldnameValue();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (ID_SELECTION_PROP.equals(propertyId)) {
            setDefaultValue((Integer) value);
        } else if (ID_FIELDNAME_PROP.equals(propertyId)) {
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
        firePropertyChange(ID_SELECTION_PROP, null, defaultValue);
    }

    public int getFieldnameValue() {
        return fieldNameValue;
    }

    public void setFieldnameValue( int fieldNameValue ) {
        this.fieldNameValue = fieldNameValue;
        firePropertyChange(ID_FIELDNAME_PROP, null, fieldNameValue);
    }

    public String getWidgetType() {
        return TYPE;
    }
}
