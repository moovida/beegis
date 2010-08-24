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
package eu.hydrologis.jgrass.featureeditor.xml.annotated;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.featureeditor.xml.annotatedguis.FormGuiElement;

/**
 * An ordered element.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public abstract class FormElement implements Comparable<FormElement> {

    /**
     * Getter for the name of the element.
     * 
     * @return the name of the element.
     */
    public abstract String getName();

    /**
     * Getter for the order of the element.
     * 
     * @return the order of the element.
     */
    public abstract int getOrder();

    @Override
    public int compareTo( FormElement o ) {
        // this ordering is not consistent with equals.
        int thisOrder = this.getOrder();
        int thatOrder = o.getOrder();

        if (thisOrder < thatOrder) {
            return -1;
        } else if (thisOrder > thatOrder) {
            return 1;
        } else
            return 0;
    }
    
    
    public FormGuiElement getGui(){
        
    }

}
