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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ACheckBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * Class representing an swt checkbox gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ACheckBoxGui extends FormGuiElement implements SelectionListener {

    private final ACheckBox checkBox;
    private SimpleFeature feature;
    private Button button;

    public ACheckBoxGui( ACheckBox checkBox ) {
        this.checkBox = checkBox;
    }

    public Control makeGui( Composite parent ) {
        button = new Button(parent, SWT.CHECK);
        button.setLayoutData(checkBox.constraints);
        // button.setText(checkBox.text);
        boolean select = false;
        if (checkBox.defaultText != null) {
            if (checkBox.defaultText.equals("1")) {
                select = true;
            } else if (checkBox.defaultText.equals("0")) {
                select = false;
            } else {
                select = new Boolean(checkBox.defaultText);
            }
        }
        button.setSelection(select);
        button.addSelectionListener(this);

        return button;
    }

    public void setFeature( SimpleFeature feature ) {
        this.feature = feature;
        updateCheck();
    }

    private void updateCheck() {
        if (feature == null) {
            return;
        }
        
        String attributeString = getAttributeString(feature, checkBox.fieldName, checkBox.defaultText);

        Boolean select = false;
        if (attributeString.equals("1")) {
            select = true;
        } else if (attributeString.equals("0")) {
            select = false;
        } else {
            select = new Boolean(attributeString);
        }

        button.setSelection(select);

    }

    public FormElement getFormElement() {
        return checkBox;
    }

    public void widgetSelected( SelectionEvent e ) {
        Class< ? > binding = feature.getProperty(checkBox.fieldName).getType().getBinding();

        boolean selection = button.getSelection();
        Object adapted = adapt(String.valueOf(selection), binding);
        if (adapted != null) {
            feature.setAttribute(checkBox.fieldName, adapted);
        }
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }
}
