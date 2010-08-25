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

import eu.hydrologis.jgrass.featureeditor.xml.annotated.CheckBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.ComboBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.Label;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.RadioButton;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.Separator;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.TextArea;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.TextField;

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

        if (formElement instanceof CheckBox) {
            CheckBox checkbox = (CheckBox) formElement;
            return new CheckBoxGui();
        } else if (formElement instanceof ComboBox) {
            ComboBox comboBox = (ComboBox) formElement;
            return new ComboBoxGui(comboBox);
        } else if (formElement instanceof Label) {
            Label label = (Label) formElement;
            return new LabelGui(label);
        } else if (formElement instanceof RadioButton) {
            RadioButton radioButton = (RadioButton) formElement;
            return new RadioButtonGui(radioButton);
        } else if (formElement instanceof Separator) {
            Separator separator = (Separator) formElement;
            return new SeparatorGui(separator);
        } else if (formElement instanceof Form) {
            Form form = (Form) formElement;
            return new FormGui(form);
        } else if (formElement instanceof TextArea) {
            TextArea textArea = (TextArea) formElement;
            return new TextAreaGui();
        } else if (formElement instanceof TextField) {
            TextField textField = (TextField) formElement;
            return new TextFieldGui(textField);
        }

        throw new IllegalArgumentException();
    }
}
