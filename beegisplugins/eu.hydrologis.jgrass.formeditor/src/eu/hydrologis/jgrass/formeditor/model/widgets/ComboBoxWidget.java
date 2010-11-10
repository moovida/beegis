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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.DEFAULT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.FIELDNAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ITEMS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_COMBOITEMS;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_DEFAULT;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_FIELDNAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_H;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_W;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_X;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_LAYOUT_Y;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_NAME;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LABELS_TAB;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.NAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ORIENTATION_TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.TAB_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.WIDTH_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.XPOS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.YPOS_PROP;

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
        ComboBoxPropertyDescriptor fields = new ComboBoxPropertyDescriptor(FIELDNAME_PROP, LABELS_FIELDNAME,
                FormEditor.getFieldNamesArrays());
        fields.setLabelProvider(new CustomLabelProviders.FieldNamesLabelProvider());
        TextPropertyDescriptor x = new TextPropertyDescriptor(XPOS_PROP, LABELS_LAYOUT_X);
        TextPropertyDescriptor y = new TextPropertyDescriptor(YPOS_PROP, LABELS_LAYOUT_Y);
        TextPropertyDescriptor w = new TextPropertyDescriptor(WIDTH_PROP, LABELS_LAYOUT_W);
        TextPropertyDescriptor h = new TextPropertyDescriptor(HEIGHT_PROP, LABELS_LAYOUT_H);
        TextPropertyDescriptor nameValue = new TextPropertyDescriptor(NAME_PROP, LABELS_NAME);
        TextPropertyDescriptor defaultValue = new TextPropertyDescriptor(DEFAULT_PROP, LABELS_DEFAULT);
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(TAB_PROP, LABELS_TAB);
        FilepathPropertyDescriptor items = new FilepathPropertyDescriptor(ITEMS_PROP, LABELS_COMBOITEMS, false, new String[]{
                "*.properties", "*.dbf"});
        descriptors = new IPropertyDescriptor[]{x, y, w, h, fields, nameValue, defaultValue, items, tabValue};

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
        if (DEFAULT_PROP.equals(propertyId)) {
            return getDefaultValue();
        } else if (ITEMS_PROP.equals(propertyId)) {
            return getItemsValue();
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            return getFieldnameValue();
        } else if (ORIENTATION_TYPE_PROP.equals(propertyId)) {
            return getTypeValue();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (DEFAULT_PROP.equals(propertyId)) {
            String defValue = (String) value;
            setDefaultValue(defValue);
        } else if (ITEMS_PROP.equals(propertyId)) {
            setItemsValue((String) value);
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            setFieldnameValue((Integer) value);
        } else if (ORIENTATION_TYPE_PROP.equals(propertyId)) {
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
        firePropertyChange(DEFAULT_PROP, null, defaultValue);
    }

    public int getTypeValue() {
        return typeValue;
    }

    public void setTypeValue( int typeValue ) {
        this.typeValue = typeValue;
        firePropertyChange(ORIENTATION_TYPE_PROP, null, typeValue);
    }

    public String getItemsValue() {
        return itemsValue;
    }

    public void setItemsValue( String itemsValue ) {
        if (itemsValue == null) {
            throw new IllegalArgumentException();
        }
        this.itemsValue = itemsValue;
        firePropertyChange(ITEMS_PROP, null, itemsValue);
    }

    public int getFieldnameValue() {
        return fieldNameValue;
    }

    public void setFieldnameValue( int fieldNameValue ) {
        this.fieldNameValue = fieldNameValue;
        firePropertyChange(FIELDNAME_PROP, null, fieldNameValue);
    }
}
