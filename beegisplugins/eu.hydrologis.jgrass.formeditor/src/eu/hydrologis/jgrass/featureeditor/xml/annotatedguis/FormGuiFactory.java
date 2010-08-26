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

    /**
     * Create the gui for the given {@link FormElement}.
     * 
     * @param formElement the form element.
     * @return teh gui for the form element.
     */
    public static FormGuiElement createFormGui( FormElement formElement ) {

        if (formElement instanceof ACheckBox) {
            ACheckBox checkbox = (ACheckBox) formElement;
            return new ACheckBoxGui();
        } else if (formElement instanceof AComboBox) {
            AComboBox comboBox = (AComboBox) formElement;
            return new AComboBoxGui(comboBox);
        } else if (formElement instanceof ALabel) {
            ALabel label = (ALabel) formElement;
            return new ALabelGui(label);
        } else if (formElement instanceof ARadioButton) {
            ARadioButton radioButton = (ARadioButton) formElement;
            return new ARadioButtonGui(radioButton);
        } else if (formElement instanceof ASeparator) {
            ASeparator separator = (ASeparator) formElement;
            return new ASeparatorGui(separator);
        } else if (formElement instanceof AForm) {
            AForm form = (AForm) formElement;
            return new AFormGui(form);
        } else if (formElement instanceof ATextArea) {
            ATextArea textArea = (ATextArea) formElement;
            return new ATextAreaGui();
        } else if (formElement instanceof ATextField) {
            ATextField textField = (ATextField) formElement;
            return new ATextFieldGui(textField);
        }

        throw new IllegalArgumentException();
    }
}
