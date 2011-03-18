/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.featureeditor.xml.annotatedguis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextField;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * Class representing an swt textfield gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ATextFieldGui extends FormGuiElement implements KeyListener {

    private final ATextField aTextField;
    private SimpleFeature feature;
    private Text text;

    public ATextFieldGui( ATextField textField ) {
        this.aTextField = textField;
    }

    @Override
    public Control makeGui( Composite parent ) {
        text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        text.setLayoutData(aTextField.constraints);
        text.addKeyListener(this);

        return text;
    }

    public void setFeature( SimpleFeature feature ) {
        this.feature = feature;
        updateTextField();
    }

    private void updateTextField() {
        if (feature == null) {
            return;
        }

        String attributeString = getAttributeString(feature, aTextField.fieldName, aTextField.defaultText);
        text.setText(attributeString);
        keyReleased(null);
    }

    public FormElement getFormElement() {
        return aTextField;
    }

    public void keyReleased( KeyEvent e ) {
        String textStr = text.getText();
        Class< ? > binding = feature.getProperty(aTextField.fieldName).getType().getBinding();

        Object adapted = adapt(textStr, binding);
        if (adapted != null) {
            feature.setAttribute(aTextField.fieldName, adapted);
        }
    }

    public void keyPressed( KeyEvent e ) {
    }

}
