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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ComboBox;

/**
 * Class representing an swt combobox gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ComboBoxGui extends FormGuiElement {
    private final ComboBox comboBox;

    public ComboBoxGui( ComboBox comboBox ) {
        this.comboBox = comboBox;
    }

    public Control makeGui( Composite parent ) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(comboBox.constraints);

        List<String> item = comboBox.item;
        String[] listArray = (String[]) item.toArray(new String[item.size()]);
        combo.setItems(listArray);
        if (comboBox.defaultText != null) {
            int index = 0;
            for( int i = 0; i < listArray.length; i++ ) {
                if (listArray[i].equals(comboBox.defaultText)) {
                    index = i;
                    break;
                }
            }
            combo.select(index);
        }
        return combo;
    }

}
