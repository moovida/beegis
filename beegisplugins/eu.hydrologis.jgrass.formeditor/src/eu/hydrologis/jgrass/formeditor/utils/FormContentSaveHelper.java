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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import eu.hydrologis.jgrass.featureeditor.utils.Utilities;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ACheckBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.AComboBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ALabel;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ARadioButton;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ASeparator;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATab;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextArea;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextField;
import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.SeparatorWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;

@SuppressWarnings("nls")
public class FormContentSaveHelper {

    private final List<AWidget> widgets;
    private final File file;

    public FormContentSaveHelper( File file, List<AWidget> widgets ) {
        this.file = file;
        this.widgets = widgets;
    }

    public void save() throws Exception {
        // divide by tab
        TreeMap<String, List<AWidget>> widgets4Tab = new TreeMap<String, List<AWidget>>();
        for( AWidget widget : widgets ) {
            String tab = widget.getTab();

            List<AWidget> list = widgets4Tab.get(tab);
            if (list == null) {
                list = new ArrayList<AWidget>();
                widgets4Tab.put(tab, list);
            }

            list.add(widget);
        }

        String[] fieldNamesArrays = FormEditor.getFieldNamesArrays();

        AForm form = new AForm();

        Set<Entry<String, List<AWidget>>> entrySet = widgets4Tab.entrySet();
        for( Entry<String, List<AWidget>> tabEntry : entrySet ) {
            String tabName = tabEntry.getKey();
            List<AWidget> widgetList = tabEntry.getValue();

            ATab tab = new ATab();
            tab.name = tabName;
            tab.text = tabName;
            tab.order = Float.parseFloat(tabName);
            tab.layoutConstraints = "insets 20 20 20 20, fill";
            // tab.colConstraints = "[left][fill][left][fill]";

            List<ACheckBox> checkboxs = tab.checkbox;
            List<AComboBox> comboboxs = tab.combobox;
            List<ALabel> labels = tab.label;
            List<ARadioButton> radiobuttons = tab.radiobutton;
            List<ASeparator> separators = tab.separator;
            List<ATextArea> textareas = tab.textarea;
            List<ATextField> textfields = tab.textfield;

            int[] rowColBounds = getRowColBounds(widgetList);
            int startRow = rowColBounds[0];
            int endRow = rowColBounds[2];

            // now define who is on which row
            float widgetIndex = 0f;
            for( int i = startRow; i < endRow + 1; i++ ) {
                List<AWidget> onThisRow = new ArrayList<AWidget>();
                for( AWidget widget : widgetList ) {
                    int[] rowBounds = widget.getRowBounds();
                    int widgetStartRow = rowBounds[0];
                    int widgetEndRow = rowBounds[1];
                    if (i >= widgetStartRow && i <= widgetEndRow) {
                        onThisRow.add(widget);
                    }
                }

                Collections.sort(onThisRow, new WidgetColSorter());

                for( int j = 0; j < onThisRow.size(); j++ ) {
                    AWidget currentWidget = onThisRow.get(j);

                    if (currentWidget.isMarked()) {
                        // TODO check how miglayout behaves
                        continue;
                    }

                    int[] colBounds = currentWidget.getColBounds();
                    int scol = colBounds[0];
                    int ecol = colBounds[1];
                    int[] rowBounds = currentWidget.getRowBounds();
                    int srow = rowBounds[0];
                    int erow = rowBounds[1];

                    StringBuilder constraintsSb = new StringBuilder();
                    if (j == 0) {
                        constraintsSb.append("skip ").append(scol).append(", ");
                    } else {
                        AWidget previousWidget = onThisRow.get(j - 1);
                        int[] colBounds2 = previousWidget.getColBounds();
                        int previousEndCol = colBounds2[1];
                        constraintsSb.append("skip ").append(scol - previousEndCol).append(", ");
                    }
                    int spanRow = erow - srow + 1;
                    int spanCol = ecol - scol + 1;
                    constraintsSb.append("span ").append(spanCol).append(" ").append(spanRow);
                    if (j == onThisRow.size() - 1) {
                        constraintsSb.append(", growx, wrap");
                    }

                    if (currentWidget instanceof LabelWidget) {
                        LabelWidget labelWidget = (LabelWidget) currentWidget;
                        ALabel label = new ALabel();
                        label.name = labelWidget.getName();
                        label.text = labelWidget.getTextValue();
                        label.order = widgetIndex++;
                        label.constraints = constraintsSb.toString();
                        labels.add(label);
                        currentWidget.setMarked(true);
                    } else if (currentWidget instanceof TextFieldWidget) {
                        TextFieldWidget textFieldWidget = (TextFieldWidget) currentWidget;
                        ATextField textField = new ATextField();
                        textField.name = textFieldWidget.getName();
                        // textField.text = textFieldWidget.getTextValue();
                        textField.defaultText = textFieldWidget.getDefaultValue();
                        textField.fieldName = fieldNamesArrays[textFieldWidget.getFieldnameValue()];
                        textField.valueType = Constants.TEXT_TYPES[textFieldWidget.getTypeValue()];
                        textField.order = widgetIndex++;
                        textField.constraints = constraintsSb.toString();
                        textfields.add(textField);
                        currentWidget.setMarked(true);
                    } else if (currentWidget instanceof SeparatorWidget) {
                        SeparatorWidget separatorWidget = (SeparatorWidget) currentWidget;
                        ASeparator separator = new ASeparator();
                        separator.name = separatorWidget.getName();
                        separator.order = widgetIndex++;
                        separator.orientation = Constants.ORIENTATION_TYPES[separatorWidget.getTypeValue()];
                        separator.constraints = constraintsSb.toString();
                        separators.add(separator);
                        currentWidget.setMarked(true);
                    }

                }

            }

            form.tab.add(tab);
        }

        for( AWidget widget : widgets ) {
            widget.setMarked(false);
        }

        Utilities.writeXML(form, file);
    }

    private int[] getRowColBounds( List<AWidget> widgetList ) {
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;

        for( AWidget aWidget : widgetList ) {
            int[] rowBounds = aWidget.getRowBounds();
            int[] colBounds = aWidget.getColBounds();

            if (rowBounds[0] < minRow) {
                minRow = rowBounds[0];
            }
            if (rowBounds[1] > maxRow) {
                maxRow = rowBounds[1];
            }
            if (colBounds[0] < minCol) {
                minCol = colBounds[0];
            }
            if (colBounds[1] < maxCol) {
                maxCol = colBounds[1];
            }
        }
        return new int[]{minRow, minCol, maxRow, maxCol};
    }

}
