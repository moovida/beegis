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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_DEFAULT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_FIELDNAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_ITEMS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_COMBOITEMS;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_DEFAULT;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_FIELDNAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_H;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_W;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_X;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_LAYOUT_Y;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_NAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_TAB;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDGET_TYPE;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_NAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_ORIENTATION_TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TAB_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TYPE_PROP;
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
 * A combobox widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ComboBoxWidget extends AWidget {
    public static final String TYPE = "combo"; //$NON-NLS-1$

    private static final long serialVersionUID = 1;

    private String defaultValue = ""; //$NON-NLS-1$
    private String itemsValue = ""; //$NON-NLS-1$
    private int fieldNameValue = 0;
    private int typeValue = 0;

    /*
     * Initializes the property descriptors array.
     */
    public ComboBoxWidget() {
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
        fields.setLabelProvider(new CustomLabelProviders.FieldNamesLabelProvider());
        TextPropertyDescriptor x = new TextPropertyDescriptor(ID_XPOS_PROP, WIDGET_LAYOUT_X);
        TextPropertyDescriptor y = new TextPropertyDescriptor(ID_YPOS_PROP, WIDGET_LAYOUT_Y);
        TextPropertyDescriptor w = new TextPropertyDescriptor(ID_WIDTH_PROP, WIDGET_LAYOUT_W);
        TextPropertyDescriptor h = new TextPropertyDescriptor(ID_HEIGHT_PROP, WIDGET_LAYOUT_H);
        TextPropertyDescriptor nameValue = new TextPropertyDescriptor(ID_NAME_PROP, WIDGET_NAME);
        TextPropertyDescriptor defaultValue = new TextPropertyDescriptor(ID_DEFAULT_PROP, WIDGET_DEFAULT);
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(ID_TAB_PROP, WIDGET_TAB);
        FilepathPropertyDescriptor items = new FilepathPropertyDescriptor(ID_ITEMS_PROP, WIDGET_COMBOITEMS, false,
                new String[]{"*.properties" //$NON-NLS-1$
                // ,"*.dbf"
                });
        descriptors = new IPropertyDescriptor[]{type, x, y, w, h, fields, nameValue, defaultValue, items, tabValue};

        addIntegerPropertyValidator(x);
        addIntegerPropertyValidator(y);
        addIntegerPropertyValidator(w);
        addIntegerPropertyValidator(h);
    }

    public Image getIcon() {
        return ImageCache.getInstance().getImage(ImageCache.COMBO_ICON_16);
    }

    public String toString() {
        return "Combo " + hashCode(); //$NON-NLS-1$
    }

    public Object getPropertyValue( Object propertyId ) {
        if (ID_DEFAULT_PROP.equals(propertyId)) {
            return getDefaultValue();
        } else if (ID_ITEMS_PROP.equals(propertyId)) {
            return getItemsValue();
        } else if (ID_FIELDNAME_PROP.equals(propertyId)) {
            return getFieldnameValue();
        } else if (ID_ORIENTATION_TYPE_PROP.equals(propertyId)) {
            return getTypeValue();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (ID_DEFAULT_PROP.equals(propertyId)) {
            String defValue = (String) value;
            setDefaultValue(defValue);
        } else if (ID_ITEMS_PROP.equals(propertyId)) {
            setItemsValue((String) value);
        } else if (ID_FIELDNAME_PROP.equals(propertyId)) {
            setFieldnameValue((Integer) value);
        } else if (ID_ORIENTATION_TYPE_PROP.equals(propertyId)) {
            setTypeValue((Integer) value);
        } else {
            super.setPropertyValue(propertyId, value);
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue( String defaultValue ) {
        if (defaultValue == null) {
            throw new IllegalArgumentException();
        }
        this.defaultValue = defaultValue;
        firePropertyChange(ID_DEFAULT_PROP, null, defaultValue);
    }

    public int getTypeValue() {
        return typeValue;
    }

    public void setTypeValue( int typeValue ) {
        this.typeValue = typeValue;
        firePropertyChange(ID_ORIENTATION_TYPE_PROP, null, typeValue);
    }

    public String getItemsValue() {
        return itemsValue;
    }

    public void setItemsValue( String itemsValue ) {
        if (itemsValue == null) {
            throw new IllegalArgumentException();
        }
        this.itemsValue = itemsValue;
        firePropertyChange(ID_ITEMS_PROP, null, itemsValue);
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
