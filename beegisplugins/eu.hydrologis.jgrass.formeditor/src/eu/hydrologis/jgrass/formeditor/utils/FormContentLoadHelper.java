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

import static eu.hydrologis.jgrass.formeditor.utils.Constants.DIMENSION_PIXEL_SNAP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LOCATION_PIXEL_SNAP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import eu.hydrologis.jgrass.featureeditor.utils.Utilities;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ALabel;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ASeparator;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATab;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextField;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;
import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.SeparatorWidget;
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
            int tabNum = Integer.parseInt(orderedTab.text);
            List< ? extends FormElement> orderedElements = orderedTab.getOrderedElements();

            int rowIndex = 0;
            int index = 0;
            int safeIndex = 0;
            while( index != orderedElements.size() || safeIndex++ == 1000 ) {
                List<FormElement> elementsInRow = new ArrayList<FormElement>();
                for( int i = index; i < orderedElements.size(); i++ ) {
                    FormElement formElement = orderedElements.get(i);
                    elementsInRow.add(formElement);
                    if (formElement.getConstraints().toLowerCase().contains("wrap")) {
                        // last in row
                        index = i + 1;
                        break;
                    }
                }

                // handle the row
                AWidget previous = null;
                for( int i = 0; i < elementsInRow.size(); i++ ) {
                    FormElement formElement = elementsInRow.get(i);
                    if (formElement instanceof ALabel) {
                        LabelWidget labelWidget = createLabelWidget(tabNum, rowIndex, previous, formElement);
                        diagram.addChild(labelWidget);
                        previous = labelWidget;
                    } else if (formElement instanceof ATextField) {
                        TextFieldWidget textFieldWidget = createTextFieldWidget(tabNum, rowIndex, previous, formElement);
                        diagram.addChild(textFieldWidget);
                        previous = textFieldWidget;
                    } else if (formElement instanceof ASeparator) {
                        SeparatorWidget separatorWidget = createSeparatorWidget(tabNum, rowIndex, previous, formElement);
                        diagram.addChild(separatorWidget);
                        previous = separatorWidget;
                    }
                }
                rowIndex++;
            }
        }

    }

    private TextFieldWidget createTextFieldWidget( int tabNum, int rowIndex, AWidget previous, FormElement formElement ) {
        ATextField textField = (ATextField) formElement;

        TextFieldWidget textFieldWidget = new TextFieldWidget();
        textFieldWidget.setTab(String.valueOf(tabNum));
        textFieldWidget.setName(textField.name);
        textFieldWidget.setDefaultValue(textField.defaultText);
        textFieldWidget.setFieldnameValue(fieldIndexFromName(textField.fieldName));
        textFieldWidget.setTypeValue(textfieldTypeFromName(textField.valueType));

        int[] xywh = findLocationAndSIze(rowIndex, previous, textField.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        textFieldWidget.setLocation(newLocation);
        textFieldWidget.setSize(newSize);
        return textFieldWidget;
    }

    private SeparatorWidget createSeparatorWidget( int tabNum, int rowIndex, AWidget previous, FormElement formElement ) {
        ASeparator separator = (ASeparator) formElement;

        SeparatorWidget separatorWidget = new SeparatorWidget();
        separatorWidget.setTab(String.valueOf(tabNum));
        separatorWidget.setName(separator.name);
        separatorWidget.setTypeValue(orientationFromName(separator.orientation));

        int[] xywh = findLocationAndSIze(rowIndex, previous, separator.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        separatorWidget.setLocation(newLocation);
        separatorWidget.setSize(newSize);
        return separatorWidget;
    }

    private LabelWidget createLabelWidget( int tabNum, int rowIndex, AWidget previous, FormElement formElement ) {
        ALabel label = (ALabel) formElement;

        LabelWidget labelWidget = new LabelWidget();
        labelWidget.setTab(String.valueOf(tabNum));
        labelWidget.setName(label.name);
        labelWidget.setTextValue(label.text);

        int[] xywh = findLocationAndSIze(rowIndex, previous, label.constraints);
        Point newLocation = new Point(xywh[0], xywh[1]);
        Dimension newSize = new Dimension(xywh[2], xywh[3]);

        labelWidget.setLocation(newLocation);
        labelWidget.setSize(newSize);
        return labelWidget;
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
     * Extracts location and size from the constraint and the previous widget.
     * 
     * @param rowIndex the current row index.
     * @param previous the prvious widget in position.
     * @param constraints the constraint string.
     * @return the array of x, y, width, height.
     */
    private int[] findLocationAndSIze( int rowIndex, AWidget previous, String constraints ) {
        int[] skipSpanxSpany = constraintToSkipSpanxSpany(constraints);
        int startRow = rowIndex;
        int heightRows = skipSpanxSpany[2];
        int startCol = skipSpanxSpany[0];
        int widthCols = skipSpanxSpany[1];

        int x = startCol * LOCATION_PIXEL_SNAP;
        int y = startRow * LOCATION_PIXEL_SNAP;
        int width = widthCols * DIMENSION_PIXEL_SNAP;
        int height = heightRows * DIMENSION_PIXEL_SNAP;
        if (previous != null) {
            Point location = previous.getLocation();
            Dimension size = previous.getSize();
            x = x + location.x + size.width;
        }
        int[] xywh = new int[]{x, y, width, height};
        return xywh;
    }

    /**
     * Converts a constraint to an array containing:
     * <ul>
     * <li>skip</li>
     * <li>spanx</li>
     * <li>spany</li>
     * </ul>
     * 
     * @param constraints the constraint string.
     * @return the array of skip, spanx, spany.
     */
    private int[] constraintToSkipSpanxSpany( String constraints ) {
        String[] constraintsArray = constraints.split(",");
        int skip = 0;
        int spanx = 1;
        int spany = 1;
        for( String constraint : constraintsArray ) {
            constraint = constraint.trim();
            if (constraint.length() == 0) {
                continue;
            }
            if (constraint.toLowerCase().startsWith("skip")) {
                String[] split = constraint.split("\\s+");
                skip = Integer.parseInt(split[1]);
            } else if (constraint.toLowerCase().startsWith("span")) {
                String[] split = constraint.split("\\s+");
                spanx = Integer.parseInt(split[1]);
                spany = Integer.parseInt(split[2]);
            }
        }
        return new int[]{skip, spanx, spany};
    }

}
