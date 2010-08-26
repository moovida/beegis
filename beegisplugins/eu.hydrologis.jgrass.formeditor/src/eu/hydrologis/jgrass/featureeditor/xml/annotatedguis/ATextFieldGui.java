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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ATextField;

/**
 * Class representing an swt textfield gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ATextFieldGui extends FormGuiElement {

    private final ATextField textField;

    public ATextFieldGui( ATextField textField ) {
        this.textField = textField;
    }

    @Override
    public Control makeGui( Composite parent ) {
        Text text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        text.setLayoutData(textField.constraints);
        if (textField.defaultText != null) {
            text.setText(textField.defaultText);
        }

        return text;
    }

}
