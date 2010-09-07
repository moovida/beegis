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
package eu.hydrologis.jgrass.featureeditor.xml.annotatedguis;

import java.util.HashMap;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ACheckBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.AComboBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ALabel;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ARadioButton;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ASeparator;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextArea;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextField;

/**
 * A factory for guis.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormGuiFactory {

    private HashMap<String, FormGuiElement> fieldNames2GuiElementsMap = new HashMap<String, FormGuiElement>();

    /**
     * Getter for the map of gui elements that do modify an attribute.
     * 
     * @return the {@link HashMap map} that maps the fieldname to the editing gui.
     */
    public HashMap<String, FormGuiElement> getFieldNames2GuiElementsMap() {
        return fieldNames2GuiElementsMap;
    }
    
    /**
     * Create the gui for the given {@link FormElement}.
     * 
     * @param formElement the form element.
     * @return the gui for the form element.
     */
    public FormGuiElement createFormGui( FormElement formElement ) {

        FormGuiElement guiElement = null;
        if (formElement instanceof ACheckBox) {
            ACheckBox checkbox = (ACheckBox) formElement;
            guiElement = new ACheckBoxGui();
        } else if (formElement instanceof AComboBox) {
            AComboBox comboBox = (AComboBox) formElement;
            guiElement = new AComboBoxGui(comboBox);
        } else if (formElement instanceof ALabel) {
            ALabel label = (ALabel) formElement;
            guiElement = new ALabelGui(label);
        } else if (formElement instanceof ARadioButton) {
            ARadioButton radioButton = (ARadioButton) formElement;
            guiElement = new ARadioButtonGui(radioButton);
        } else if (formElement instanceof ASeparator) {
            ASeparator separator = (ASeparator) formElement;
            guiElement = new ASeparatorGui(separator);
        } else if (formElement instanceof ATextArea) {
            ATextArea textArea = (ATextArea) formElement;
            guiElement = new ATextAreaGui();
        } else if (formElement instanceof ATextField) {
            ATextField textField = (ATextField) formElement;
            guiElement = new ATextFieldGui(textField);
        }
        if (guiElement != null) {
            String fieldName = guiElement.getFormElement().getFieldName();
            if (fieldName != null) {
                fieldNames2GuiElementsMap.put(fieldName, guiElement);
            }
            return guiElement;
        }
        throw new IllegalArgumentException();
    }
}
