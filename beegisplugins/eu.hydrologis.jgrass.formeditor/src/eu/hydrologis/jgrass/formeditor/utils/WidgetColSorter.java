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
package eu.hydrologis.jgrass.formeditor.utils;

import java.util.Comparator;

import eu.hydrologis.jgrass.formeditor.model.AWidget;

/**
 * Sorts the widgets along a row based on the columns.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class WidgetColSorter implements Comparator<AWidget> {

    public int compare( AWidget o1, AWidget o2 ) {
        int[] cb1 = o1.getColBounds();
        int[] cb2 = o2.getColBounds();

        if (cb1[1] <= cb2[0]) {
            return -1;
        } else if (cb2[1] <= cb1[0]) {
            return 1;
        } else {
            return 0;
        }
    }

}
