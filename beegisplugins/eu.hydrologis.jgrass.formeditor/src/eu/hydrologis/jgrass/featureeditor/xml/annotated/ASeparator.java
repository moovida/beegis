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
package eu.hydrologis.jgrass.featureeditor.xml.annotated;

import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.CONSTRAINTS;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.NAME;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.ORDER;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.ORIENTATION;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.SEPARATOR;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class representing an swt separator label.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@XmlRootElement(name = SEPARATOR)
public class ASeparator extends FormElement {

    /**
     * Unique name for the object.
     */
    @XmlAttribute(name = NAME)
    public String name = null;

    /**
     * The widget order.
     */
    @XmlAttribute(name = ORDER)
    public Float order = null;

    /**
     * The separator orientation.
     */
    @XmlAttribute(name = ORIENTATION)
    public String orientation = null;

    /**
     * The layout constraints.
     */
    @XmlAttribute(name = CONSTRAINTS)
    public String constraints = null;

    @Override
    public float getOrder() {
        if (order == null) {
            order = 0f;
        }
        return order;
    }

    @Override
    public String getName() {
        return name;
    }
    
    public String getFieldName() {
        return null;
    }
    
    public String getConstraints() {
        return constraints;
    }

    public void setConstraints( String constraints ) {
        this.constraints = constraints;
    }
}
