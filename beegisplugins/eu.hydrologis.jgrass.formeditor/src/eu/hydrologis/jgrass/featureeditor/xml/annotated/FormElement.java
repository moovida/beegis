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


/**
 * An ordered form element.
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
     * Getter for the filed name of the element.
     * 
     * @return the field name of the element.
     */
    public abstract String getFieldName();

    /**
     * Getter for the order of the element.
     * 
     * <p>The order element is of type float to allow tweaks without 
     * the need to rewrite whole piles of following order tags.
     * 
     * @return the order of the element.
     */
    public abstract float getOrder();

    @Override
    public int compareTo( FormElement o ) {
        // this ordering is not consistent with equals.
        float thisOrder = this.getOrder();
        float thatOrder = o.getOrder();

        if (thisOrder < thatOrder) {
            return -1;
        } else if (thisOrder > thatOrder) {
            return 1;
        } else
            return 0;
    }
}
