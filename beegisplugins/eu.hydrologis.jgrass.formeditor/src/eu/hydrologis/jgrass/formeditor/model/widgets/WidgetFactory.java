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
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_DEFAULT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_ITEMS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_LOCATION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_NAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_SELECTION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_SIZE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TEXT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TYPE_PROP;

import java.util.Properties;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import eu.hydrologis.jgrass.formeditor.model.AWidget;

/**
 * A factory for all the widgets.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class WidgetFactory {

    public static AWidget createWidget( Properties properties ) {
        String type = properties.getProperty(ID_TYPE_PROP);
        String fieldName = properties.getProperty(ID_NAME_PROP);
        String sizeString = properties.getProperty(ID_SIZE_PROP);
        String[] sizeSplit = sizeString.split(","); //$NON-NLS-1$
        int width = Integer.parseInt(sizeSplit[0].trim());
        int height = Integer.parseInt(sizeSplit[1].trim());

        String locationString = properties.getProperty(ID_LOCATION_PROP);
        String[] locSplit = locationString.split(","); //$NON-NLS-1$
        int x = Integer.parseInt(locSplit[0].trim());
        int y = Integer.parseInt(locSplit[1].trim());

        AWidget widget = null;
        if (type.equals(TextFieldWidget.TYPE)) {
            widget = new TextFieldWidget();
            addTextFieldAttributes((TextFieldWidget) widget, properties);
        } else if (type.equals(TextAreaWidget.TYPE)) {
            widget = new TextAreaWidget();
            addTextAreaAttributes((TextAreaWidget) widget, properties);
        } else if (type.equals(LabelWidget.TYPE)) {
            widget = new LabelWidget();
            addLabelAttributes((LabelWidget) widget, properties);
        } else if (type.equals(SeparatorWidget.TYPE)) {
            widget = new SeparatorWidget();
            addSeparatorAttributes((SeparatorWidget) widget, properties);
        } else if (type.equals(ComboBoxWidget.TYPE)) {
            widget = new ComboBoxWidget();
            addComboBoxAttributes((ComboBoxWidget) widget, properties);
        } else if (type.equals(CheckBoxWidget.TYPE)) {
            widget = new CheckBoxWidget();
            addCheckBoxAttributes((CheckBoxWidget) widget, properties);
        } else if (type.equals(RadioButtonWidget.TYPE)) {
            widget = new RadioButtonWidget();
            addRadioButtonAttributes((RadioButtonWidget) widget, properties);
        } else {
            throw new IllegalArgumentException();
        }

        // add common attributes
        widget.setName(fieldName);
        Point point = new Point(x, y);
        widget.setLocation(point);
        Dimension dim = new Dimension(width, height);
        widget.setSize(dim);

        return widget;
    }

    private static void addRadioButtonAttributes( RadioButtonWidget widget, Properties properties ) {
        String itemsString = properties.getProperty(ID_ITEMS_PROP);
        widget.setItemsValue(itemsString);
        String defaultString = properties.getProperty(ID_DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addCheckBoxAttributes( CheckBoxWidget widget, Properties properties ) {
        String selectionString = properties.getProperty(ID_SELECTION_PROP);
        if (selectionString.equals(CHECKBOX_TYPES[0])) {
            widget.setDefaultValue(0);
        } else {
            widget.setDefaultValue(1);
        }
    }

    private static void addComboBoxAttributes( ComboBoxWidget widget, Properties properties ) {
        String itemsString = properties.getProperty(ID_ITEMS_PROP);
        widget.setItemsValue(itemsString);
        String defaultString = properties.getProperty(ID_DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addTextFieldAttributes( TextFieldWidget widget, Properties properties ) {
        String defaultString = properties.getProperty(ID_DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addTextAreaAttributes( TextAreaWidget widget, Properties properties ) {
        String defaultString = properties.getProperty(ID_DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addLabelAttributes( LabelWidget widget, Properties properties ) {
        String textString = properties.getProperty(ID_TEXT_PROP);
        widget.setText(textString);
    }

    private static void addSeparatorAttributes( SeparatorWidget widget, Properties properties ) {
    }

}
