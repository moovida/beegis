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
package eu.hydrologis.jgrass.featureeditor.xml.annotatedguis;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * A gui element.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public abstract class FormGuiElement {

    /**
     * Creates the gui for the element basing on the {@link MigLayout}.
     * 
     * @param parent the parent {@link Composite}, assuming that it has a {@link MigLayout}.
     * @return the created control.
     */
    public abstract Control makeGui( Composite parent );

    /**
     * Set the {@link SimpleFeature feature} on which to act.
     * 
     * @param feature the feature to modify.
     */
    public abstract void setFeature( SimpleFeature feature );

    /**
     * Getter for the {@link FormElement} that generated that gui element.
     * 
     * @return the {@link FormElement} that generated that gui element.
     */
    public abstract FormElement getFormElement();

    /**
     * Tries to convert a string to the required class.
     * 
     * @param value the string to convert.
     * @param clazz the class to which convert.
     * @return the new object or <code>null</code> if it can't adapt.
     */
    public <T> T adapt( String value, Class<T> clazz ) {
        try {
            if (clazz.isAssignableFrom(Double.class)) {
                Double parsedDouble = new Double(value);
                return clazz.cast(parsedDouble);
            } else if (clazz.isAssignableFrom(Float.class)) {
                Float parsedFloat = new Float(value);
                return clazz.cast(parsedFloat);
            } else if (clazz.isAssignableFrom(Integer.class)) {
                Integer parsedInteger = null;
                try {
                    parsedInteger = new Integer(value);
                } catch (Exception e) {
                    // try also true/false
                    if (value.toLowerCase().equals("true") || value.toLowerCase().equals("y")) {
                        parsedInteger = 1;
                    } else if (value.toLowerCase().equals("false") || value.toLowerCase().equals("n")) {
                        parsedInteger = 0;
                    } else {
                        return null;
                    }
                }
                return clazz.cast(parsedInteger);
            } else if (clazz.isAssignableFrom(Long.class)) {
                Long parsedLong = new Long(value);
                return clazz.cast(parsedLong);
            } else if (clazz.isAssignableFrom(String.class)) {
                return clazz.cast(value);
            }
        } catch (Exception e) {
            // ignore, if it can't resolve, return null
        }
        return null;
    }

    protected String getAttributeString( SimpleFeature feature, String fieldName, String defaultValue ) {
        Object attribute = feature.getAttribute(fieldName);
        String attributeString = "";
        if (attribute != null) {
            attributeString = attribute.toString();
        }
        if (attributeString.equals("")) {
            if (defaultValue != null) {
                attributeString = defaultValue;
            } else {
                AttributeDescriptor descriptor = feature.getFeatureType().getDescriptor(fieldName);
                attributeString = descriptor.getDefaultValue().toString();
            }
        }
        return attributeString;
    }

}
