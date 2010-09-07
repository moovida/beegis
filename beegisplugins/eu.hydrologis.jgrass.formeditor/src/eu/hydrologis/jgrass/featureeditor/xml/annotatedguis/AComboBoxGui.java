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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.AComboBox;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * Class representing an swt combobox gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class AComboBoxGui extends FormGuiElement implements SelectionListener {
    private final AComboBox aComboBox;
    private SimpleFeature feature;
    private Combo combo;
    private List<String> labelList;
    private List<String> valuesList;

    public AComboBoxGui( AComboBox comboBox ) {
        this.aComboBox = comboBox;
    }

    public Control makeGui( Composite parent ) {
        combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(aComboBox.constraints);

        labelList = new ArrayList<String>();
        valuesList = new ArrayList<String>();

        List<String> itemList = aComboBox.item;
        for( String item : itemList ) {
            if (item.indexOf(',') != -1) {
                String[] split = item.split(","); //$NON-NLS-1$
                labelList.add(split[0].trim());
                valuesList.add(split[1].trim());
            } else {
                labelList.add(item);
                valuesList.add(item);
            }
        }

        String[] listArray = (String[]) labelList.toArray(new String[labelList.size()]);
        combo.setItems(listArray);
        if (aComboBox.defaultText != null) {
            int index = 0;
            for( int i = 0; i < valuesList.size(); i++ ) {
                String value = valuesList.get(i);
                if (value.equals(aComboBox.defaultText)) {
                    index = i;
                    break;
                }
            }
            combo.select(index);
        }
        combo.addSelectionListener(this);
        return combo;
    }

    public void setFeature( SimpleFeature feature ) {
        this.feature = feature;
        updateCombo();
    }

    private void updateCombo() {
        if (feature == null) {
            return;
        }
        Object attribute = feature.getAttribute(aComboBox.fieldName);
        String attributeString = attribute.toString();

        int index = valuesList.indexOf(attributeString);
        if (index == -1) {
            // set the default value if available
            if (aComboBox.defaultText != null && aComboBox.defaultText.length() > 0) {
                index = valuesList.indexOf(aComboBox.defaultText);
            }
        }
        if (index == -1) {
            index = 0;
        }
        combo.select(index);
        widgetSelected(null);
    }

    public FormElement getFormElement() {
        return aComboBox;
    }

    public void widgetSelected( SelectionEvent e ) {
        int selectionIndex = combo.getSelectionIndex();
        String comboStr = combo.getItem(selectionIndex);
        Class< ? > binding = feature.getProperty(aComboBox.fieldName).getType().getBinding();

        int index = labelList.indexOf(comboStr);
        String valueStr = valuesList.get(index);

        Object adapted = adapt(valueStr, binding);
        if (adapted != null) {
            feature.setAttribute(aComboBox.fieldName, adapted);
        }
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }

}
