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

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.opengis.feature.type.AttributeDescriptor;

import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.utils.Constants;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.*;

/**
 * A textfield widget.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TextFieldWidget extends AWidget {
    public static final String TYPE = "text";

    private static final long serialVersionUID = 1;

    private String defaultValue = "";
    private int typeValue = 0;
    private int fieldNameValue = 0;

    public TextFieldWidget() {
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
        fields.setLabelProvider(new FieldNamesLabelProvider());
        TextPropertyDescriptor x = new TextPropertyDescriptor(XPOS_PROP, LABELS_LAYOUT_X);
        TextPropertyDescriptor y = new TextPropertyDescriptor(YPOS_PROP, LABELS_LAYOUT_Y);
        TextPropertyDescriptor w = new TextPropertyDescriptor(WIDTH_PROP, LABELS_LAYOUT_W);
        TextPropertyDescriptor h = new TextPropertyDescriptor(HEIGHT_PROP, LABELS_LAYOUT_H);
        TextPropertyDescriptor nameValue = new TextPropertyDescriptor(NAME_PROP, LABELS_NAME);
        TextPropertyDescriptor defaultValue = new TextPropertyDescriptor(DEFAULT_PROP, LABELS_DEFAULT);
        ComboBoxPropertyDescriptor types = new ComboBoxPropertyDescriptor(TEXT_TYPE_PROP, LABELS_TEXT_TYPE, Constants.TEXT_TYPES);
        types.setLabelProvider(new TypesLabelProvider());
        TextPropertyDescriptor tabValue = new TextPropertyDescriptor(TAB_PROP, LABELS_TAB);
        descriptors = new IPropertyDescriptor[]{x, y, w, h, fields, nameValue, defaultValue, types, tabValue};

        addIntegerPropertyValidator(x);
        addIntegerPropertyValidator(y);
        addIntegerPropertyValidator(w);
        addIntegerPropertyValidator(h);
        addIntegerPropertyValidator(tabValue);
    }
    static private class TypesLabelProvider extends LabelProvider {
        public String getText( Object element ) {
            return Constants.TEXT_TYPES[((Integer) element).intValue()];
        }
    }
    static private class FieldNamesLabelProvider extends LabelProvider {
        public String getText( Object element ) {
            return FormEditor.getFieldNamesArrays()[((Integer) element).intValue()];
        }
    }

    public Image getIcon() {
        return ImageCache.getInstance().getImage(ImageCache.TEXT_ICON_16);
    }

    public String toString() {
        return "Textfield " + hashCode();
    }

    public Object getPropertyValue( Object propertyId ) {
        if (DEFAULT_PROP.equals(propertyId)) {
            return getDefaultValue();
        } else if (TEXT_TYPE_PROP.equals(propertyId)) {
            return getTypeValue();
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            return getFieldnameValue();
        }
        return super.getPropertyValue(propertyId);
    }

    public void setPropertyValue( Object propertyId, Object value ) {
        if (DEFAULT_PROP.equals(propertyId)) {
            String defValue = (String) value;
            setDefaultValue(defValue);
        } else if (TEXT_TYPE_PROP.equals(propertyId)) {
            setTypeValue((Integer) value);
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            setFieldnameValue((Integer) value);
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
        firePropertyChange(TEXT_TYPE_PROP, null, typeValue);
    }

    public int getFieldnameValue() {
        return fieldNameValue;
    }

    public void setFieldnameValue( int fieldNameValue ) {
        this.fieldNameValue = fieldNameValue;
        firePropertyChange(FIELDNAME_PROP, null, fieldNameValue);
    }

    public String toDumpString() {
        String tmpName = getName().replaceAll("\\s+", "_");
        Dimension tmpSize = getSize();
        Point tmpLocation = getLocation();
        String tmpDefaultValue = getDefaultValue();

        StringBuilder sB = new StringBuilder();
        sB.append(tmpName).append(".").append(TYPE_PROP).append("=");
        sB.append(TYPE).append("\n");
        sB.append(tmpName).append(".").append(FIELDNAME_PROP).append("=");
        sB.append(tmpName).append("\n");
        sB.append(tmpName).append(".").append(SIZE_PROP).append("=");
        sB.append(tmpSize.width).append(",").append(tmpSize.height).append("\n");
        sB.append(tmpName).append(".").append(LOCATION_PROP).append("=");
        sB.append(tmpLocation.x).append(",").append(tmpLocation.y).append("\n");
        sB.append(tmpName).append(".").append(DEFAULT_PROP).append("=");
        sB.append(tmpDefaultValue).append("\n");

        return sB.toString();
    }

}
