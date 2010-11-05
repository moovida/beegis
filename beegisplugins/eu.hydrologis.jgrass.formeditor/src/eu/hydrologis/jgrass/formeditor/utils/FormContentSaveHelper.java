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
import eu.hydrologis.jgrass.formeditor.model.widgets.CheckBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.SeparatorWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextAreaWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;

/**
 * The class that takes care of dumping the {@link AWidget widgets} to disk in proper XML format.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class FormContentSaveHelper {

    private final List<AWidget> widgets;
    private final File file;

    /**
     * Constructor.
     * 
     * @param file the file to which to save the widgets.
     * @param widgets the widgets to dump.
     */
    public FormContentSaveHelper( File file, List<AWidget> widgets ) {
        this.file = file;
        this.widgets = widgets;
    }

    /**
     * Converts the {@link AWidget widgets} to XML and dumps it to file.
     * 
     * @throws Exception
     */
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
            // also clean them
            widget.setMarked(false);
        }

        String[] fieldNamesArrays = FormEditor.getFieldNamesArrays();

        AForm form = new AForm();

        float tabIndex = 0;
        Set<Entry<String, List<AWidget>>> entrySet = widgets4Tab.entrySet();
        for( Entry<String, List<AWidget>> tabEntry : entrySet ) {
            String tabName = tabEntry.getKey();
            List<AWidget> widgetList = tabEntry.getValue();

            ATab tab = new ATab();
            tab.name = tabName;
            tab.text = tabName;
            tab.order = tabIndex++;
            tab.layoutConstraints = "insets 20 20 20 20, fill";
            // tab.colConstraints = "[left][fill][left][fill]";

            List<ACheckBox> checkboxs = tab.checkbox;
            List<AComboBox> comboboxs = tab.combobox;
            List<ALabel> labels = tab.label;
            List<ARadioButton> radiobuttons = tab.radiobutton;
            List<ASeparator> separators = tab.separator;
            List<ATextArea> textareas = tab.textarea;
            List<ATextField> textfields = tab.textfield;

            // now define who is on which row
            float widgetIndex = 0f;
            for( AWidget widget : widgetList ) {
                int[] rowBounds = widget.getRowBounds();
                int[] colBounds = widget.getColBounds();
                int widgetStartRow = rowBounds[0];
                int widgetEndRow = rowBounds[1];
                int widgetStartCol = colBounds[0];
                int widgetEndCol = colBounds[1];

                if (widget instanceof LabelWidget) {
                    LabelWidget labelWidget = (LabelWidget) widget;
                    ALabel label = new ALabel();
                    label.name = labelWidget.getName();
                    label.text = labelWidget.getText();
                    label.order = widgetIndex++;
                    label.constraints = "cell " + widgetStartCol + " " + widgetStartRow + " "
                            + (widgetEndCol - widgetStartCol + 1) + " " + (widgetEndRow - widgetStartRow + 1) + ", growx";
                    labels.add(label);
                } else if (widget instanceof TextFieldWidget) {
                    TextFieldWidget textFieldWidget = (TextFieldWidget) widget;
                    ATextField textField = new ATextField();
                    textField.name = textFieldWidget.getName();
                    // textField.text = textFieldWidget.getTextValue();
                    textField.defaultText = textFieldWidget.getDefaultValue();
                    textField.fieldName = fieldNamesArrays[textFieldWidget.getFieldnameValue()];
                    textField.valueType = Constants.TEXT_TYPES[textFieldWidget.getTypeValue()];
                    textField.order = widgetIndex++;
                    textField.constraints = "cell " + widgetStartCol + " " + widgetStartRow + " "
                            + (widgetEndCol - widgetStartCol + 1) + " " + (widgetEndRow - widgetStartRow + 1) + ", growx";
                    textfields.add(textField);
                } else if (widget instanceof TextAreaWidget) {
                    TextAreaWidget textAreaWidget = (TextAreaWidget) widget;
                    ATextArea textArea = new ATextArea();
                    textArea.name = textAreaWidget.getName();
                    // textField.text = textFieldWidget.getTextValue();
                    textArea.defaultText = textAreaWidget.getDefaultValue();
                    textArea.fieldName = fieldNamesArrays[textAreaWidget.getFieldnameValue()];
                    textArea.order = widgetIndex++;
                    textArea.constraints = "cell " + widgetStartCol + " " + widgetStartRow + " "
                            + (widgetEndCol - widgetStartCol + 1) + " " + (widgetEndRow - widgetStartRow + 1) + ", growx, growy";
                    textareas.add(textArea);
                } else if (widget instanceof SeparatorWidget) {
                    SeparatorWidget separatorWidget = (SeparatorWidget) widget;
                    ASeparator separator = new ASeparator();
                    separator.name = separatorWidget.getName();
                    separator.order = widgetIndex++;
                    separator.orientation = Constants.ORIENTATION_TYPES[separatorWidget.getTypeValue()];
                    separator.constraints = "cell " + widgetStartCol + " " + widgetStartRow + " "
                            + (widgetEndCol - widgetStartCol + 1) + " " + (widgetEndRow - widgetStartRow + 1) + ", growx";
                    separators.add(separator);
                } else if (widget instanceof CheckBoxWidget) {
                    CheckBoxWidget checkboxWidget = (CheckBoxWidget) widget;
                    ACheckBox checkboxField = new ACheckBox();
                    checkboxField.name = checkboxWidget.getName();
                    checkboxField.fieldName = fieldNamesArrays[checkboxWidget.getFieldnameValue()];
                    checkboxField.defaultText = Constants.CHECKBOX_TYPES[checkboxWidget.getDefaultValue()];
                    checkboxField.order = widgetIndex++;
                    checkboxField.constraints = "cell " + widgetStartCol + " " + widgetStartRow + " "
                            + (widgetEndCol - widgetStartCol + 1) + " " + (widgetEndRow - widgetStartRow + 1) + ", growx";
                    checkboxs.add(checkboxField);
                }

            }

            form.tab.add(tab);
        }

        Utilities.writeXML(form, file);
    }

}
