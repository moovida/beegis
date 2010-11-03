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
package eu.hydrologis.jgrass.formeditor.model.widgets;

import org.eclipse.jface.viewers.LabelProvider;

import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.utils.Constants;

/**
 * {@link LabelProvider}s for properties support.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class CustomLabelProviders {
    static class TypesLabelProvider extends LabelProvider {
        public String getText( Object element ) {
            return Constants.TEXT_TYPES[((Integer) element).intValue()];
        }
    }
    static class OrientationTypesLabelProvider extends LabelProvider {
        public String getText( Object element ) {
            return Constants.ORIENTATION_TYPES[((Integer) element).intValue()];
        }
    }
    static class FieldNamesLabelProvider extends LabelProvider {
        public String getText( Object element ) {
            return FormEditor.getFieldNamesArrays()[((Integer) element).intValue()];
        }
    }
}
