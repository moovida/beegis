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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ARadioButton;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * Class representing an swt combobox gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ARadioButtonGui extends FormGuiElement implements SelectionListener {
    private final ARadioButton aRadioButton;
    private SimpleFeature feature;
    private List<String> labelList;
    private List<String> valuesList;
    private List<Button> buttonsList;

    public ARadioButtonGui( ARadioButton radioButton ) {
        this.aRadioButton = radioButton;
    }

    @Override
    public Control makeGui( Composite parent ) {

        Composite radioComposite = new Composite(parent, SWT.NONE);
        radioComposite.setLayoutData(aRadioButton.constraints);

        if (aRadioButton.orientation == null || aRadioButton.orientation.toLowerCase().startsWith("ver")) {
            radioComposite.setLayout(new GridLayout(1, false));
        } else {
            radioComposite.setLayout(new GridLayout(aRadioButton.item.size(), false));
        }
        labelList = new ArrayList<String>();
        valuesList = new ArrayList<String>();
        buttonsList = new ArrayList<Button>();

        List<String> itemsList = aRadioButton.item;
        for( String item : itemsList ) {
            if (item.indexOf(',') != -1) {
                String[] split = item.split(","); //$NON-NLS-1$
                labelList.add(split[0].trim());
                valuesList.add(split[1].trim());
            } else {
                labelList.add(item);
                valuesList.add(item);
            }
        }

        for( String radioText : labelList ) {
            Button button = new Button(radioComposite, SWT.RADIO);
            button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            button.setText(radioText);
            if (aRadioButton.defaultText != null && aRadioButton.defaultText.equals(radioText)) {
                button.setSelection(true);
            }
            button.addSelectionListener(this);
            buttonsList.add(button);
        }

        return radioComposite;
    }

    public void setFeature( SimpleFeature feature ) {
        this.feature = feature;
        updateRadio();
    }

    private void updateRadio() {
        if (feature == null) {
            return;
        }
        Object attribute = feature.getAttribute(aRadioButton.fieldName);
        String attributeString = attribute.toString();

        // find the button index and select it
        int index = valuesList.indexOf(attributeString);
        if (index == -1) {
            index = 0;
        }

        for( int i = 0; i < buttonsList.size(); i++ ) {
            Button button = buttonsList.get(i);
            if (i == index) {
                button.setSelection(true);
            } else {
                button.setSelection(false);
            }
        }

    }

    public FormElement getFormElement() {
        return aRadioButton;
    }

    public void widgetSelected( SelectionEvent e ) {
        Button source = (Button) e.getSource();

        int index = buttonsList.indexOf(source);
        String value = valuesList.get(index);
        Class< ? > binding = feature.getProperty(aRadioButton.fieldName).getType().getBinding();

        Object adapted = adapt(value, binding);
        if (adapted != null) {
            feature.setAttribute(aRadioButton.fieldName, adapted);
        }
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }
}
