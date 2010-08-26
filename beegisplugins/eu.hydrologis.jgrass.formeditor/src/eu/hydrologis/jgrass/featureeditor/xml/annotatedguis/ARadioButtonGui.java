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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ARadioButton;

/**
 * Class representing an swt combobox gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ARadioButtonGui extends FormGuiElement {
    private final ARadioButton radioButton;

    public ARadioButtonGui( ARadioButton radioButton ) {
        this.radioButton = radioButton;

    }

    @Override
    public Control makeGui( Composite parent ) {

        Composite radioComposite = new Composite(parent, SWT.NONE);
        radioComposite.setLayoutData(radioButton.constraints);

        if (radioButton.orientation == null || radioButton.orientation.toLowerCase().startsWith("ver")) {
            radioComposite.setLayout(new GridLayout(1, false));
        } else {
            radioComposite.setLayout(new GridLayout(radioButton.item.size(), false));
        }

        for( String radioText : radioButton.item ) {
            Button button = new Button(radioComposite, SWT.RADIO);
            button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            button.setText(radioText);
            if (radioButton.defaultText != null && radioButton.defaultText.equals(radioText)) {
                button.setSelection(true);
            }
        }

        return radioComposite;
    }
}
