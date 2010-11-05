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
package eu.hydrologis.jgrass.formeditor.utils;

import static eu.hydrologis.jgrass.formeditor.utils.Constants.CHECKBOX_TYPES;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.DIMENSION_PIXEL_SNAP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LOCATION_PIXEL_SNAP;

import java.io.File;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import eu.hydrologis.jgrass.featureeditor.utils.Utilities;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ACheckBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ALabel;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ASeparator;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATab;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextArea;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextField;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;
import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;
import eu.hydrologis.jgrass.formeditor.model.widgets.CheckBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.SeparatorWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextAreaWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;

/**
 * The class that takes care of loading the XML file into {@link AWidget widgets}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
@SuppressWarnings("nls")
public class FormContentLoadHelper {

    private final File file;
    private final WidgetsDiagram diagram;

    /**
     * Constructor.
     * 
     * @param file the file from which to load the XML.
     * @param diagram the {@link WidgetsDiagram diagram} to which to add the widgets.
     */
    public FormContentLoadHelper( File file, WidgetsDiagram diagram ) {
        this.file = file;
        this.diagram = diagram;
    }

    /**
     * Creates {@link AWidget widgets} from the XML file and adds them to teh main diagram.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        AForm form = Utilities.readForm(file);

        // for every Tab object create a tab
        List<ATab> orderedTabs = form.getOrderedTabs();
        for( ATab orderedTab : orderedTabs ) {
            String tabName = orderedTab.text;
            List< ? extends FormElement> orderedElements = orderedTab.getOrderedElements();

            for( FormElement formElement : orderedElements ) {
                if (formElement instanceof ALabel) {
                    LabelWidget labelWidget = createLabelWidget(tabName, formElement);
                    diagram.addChild(labelWidget);
                } else if (formElement instanceof ATextField) {
                    TextFieldWidget textFieldWidget = createTextFieldWidget(tabName, formElement);
                    diagram.addChild(textFieldWidget);
                } else if (formElement instanceof ATextArea) {
                    TextAreaWidget textAreaWidget = createTextAreaWidget(tabName, formElement);
                    diagram.addChild(textAreaWidget);
                } else if (formElement instanceof ASeparator) {
                    SeparatorWidget separatorWidget = createSeparatorWidget(tabName, formElement);
                    diagram.addChild(separatorWidget);
                } else if (formElement instanceof ACheckBox) {
                    CheckBoxWidget checkboxWidget = createCheckBoxWidget(tabName, formElement);
                    diagram.addChild(checkboxWidget);
                }

            }

        }

    }

    private TextFieldWidget createTextFieldWidget( String tabName, FormElement formElement ) {
        ATextField textField = (ATextField) formElement;

        TextFieldWidget textFieldWidget = new TextFieldWidget();
        textFieldWidget.setTab(tabName);
        textFieldWidget.setName(textField.name);
        textFieldWidget.setDefaultValue(textField.defaultText);
        textFieldWidget.setFieldnameValue(fieldIndexFromName(textField.fieldName));
        textFieldWidget.setTypeValue(textfieldTypeFromName(textField.valueType));

        int[] xywh = findLocationAndSize(textField.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        textFieldWidget.setLocation(newLocation);
        textFieldWidget.setSize(newSize);
        return textFieldWidget;
    }

    private TextAreaWidget createTextAreaWidget( String tabName, FormElement formElement ) {
        ATextArea textArea = (ATextArea) formElement;

        TextAreaWidget textAreaWidget = new TextAreaWidget();
        textAreaWidget.setTab(tabName);
        textAreaWidget.setName(textArea.name);
        textAreaWidget.setDefaultValue(textArea.defaultText);
        textAreaWidget.setFieldnameValue(fieldIndexFromName(textArea.fieldName));

        int[] xywh = findLocationAndSize(textArea.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        textAreaWidget.setLocation(newLocation);
        textAreaWidget.setSize(newSize);
        return textAreaWidget;
    }

    private SeparatorWidget createSeparatorWidget( String tabName, FormElement formElement ) {
        ASeparator separator = (ASeparator) formElement;

        SeparatorWidget separatorWidget = new SeparatorWidget();
        separatorWidget.setTab(tabName);
        separatorWidget.setName(separator.name);
        separatorWidget.setTypeValue(orientationFromName(separator.orientation));

        int[] xywh = findLocationAndSize(separator.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        separatorWidget.setLocation(newLocation);
        separatorWidget.setSize(newSize);
        return separatorWidget;
    }

    private LabelWidget createLabelWidget( String tabName, FormElement formElement ) {
        ALabel label = (ALabel) formElement;

        LabelWidget labelWidget = new LabelWidget();
        labelWidget.setTab(tabName);
        labelWidget.setName(label.name);
        labelWidget.setTextValue(label.text);

        int[] xywh = findLocationAndSize(label.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        labelWidget.setLocation(newLocation);
        labelWidget.setSize(newSize);
        return labelWidget;
    }
    

    private CheckBoxWidget createCheckBoxWidget( String tabName, FormElement formElement ) {
        ACheckBox checkbox = (ACheckBox) formElement;

        CheckBoxWidget checkBoxWidgetWidget = new CheckBoxWidget();
        checkBoxWidgetWidget.setTab(tabName);
        checkBoxWidgetWidget.setName(checkbox.name);
        if (checkbox.defaultText.equals(CHECKBOX_TYPES[0])) {
            checkBoxWidgetWidget.setDefaultValue(0);
        } else {
            checkBoxWidgetWidget.setDefaultValue(1);
        }
        checkBoxWidgetWidget.setFieldnameValue(fieldIndexFromName(checkbox.fieldName));

        int[] xywh = findLocationAndSize(checkbox.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        checkBoxWidgetWidget.setLocation(newLocation);
        checkBoxWidgetWidget.setSize(newSize);
        return checkBoxWidgetWidget;
    }

    private int fieldIndexFromName( String name ) {
        String[] fieldNamesArrays = FormEditor.getFieldNamesArrays();
        for( int i = 0; i < fieldNamesArrays.length; i++ ) {
            if (fieldNamesArrays[i].equals(name)) {
                return i;
            }
        }
        return 0;
    }

    private int textfieldTypeFromName( String name ) {
        for( int i = 0; i < Constants.TEXT_TYPES.length; i++ ) {
            if (Constants.TEXT_TYPES[i].equals(name)) {
                return i;
            }
        }
        return 0;
    }

    private int orientationFromName( String name ) {
        for( int i = 0; i < Constants.ORIENTATION_TYPES.length; i++ ) {
            if (Constants.ORIENTATION_TYPES[i].equals(name)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Extracts location and size from the constraint.
     * 
     * @param constraints the constraint string.
     * @return the array of x, y, width, height.
     */
    private int[] findLocationAndSize( String constraints ) {
        int startCol = 0;
        int startRow = 0;
        int widthCols = 0;
        int heightRows = 0;

        String[] constraintsArray = constraints.split(",");
        for( String constraint : constraintsArray ) {
            constraint = constraint.trim();
            if (constraint.toLowerCase().startsWith("cell")) {
                String[] split = constraint.split("\\s+");
                startCol = Integer.parseInt(split[1]);
                startRow = Integer.parseInt(split[2]);
                widthCols = Integer.parseInt(split[3]);
                heightRows = Integer.parseInt(split[4]);
            }
        }

        int x = startCol * LOCATION_PIXEL_SNAP;
        int y = startRow * LOCATION_PIXEL_SNAP;
        int width = widthCols * DIMENSION_PIXEL_SNAP;
        int height = heightRows * DIMENSION_PIXEL_SNAP;

        int[] xywh = new int[]{x, y, width, height};
        return xywh;
    }

}
