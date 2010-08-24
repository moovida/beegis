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
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.LABEL;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.NAME;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.ORDER;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.TEXT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Class representing an swt label.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@XmlRootElement(name = LABEL)
public class Label extends FormElement {

    /**
     * Unique name for the object.
     */
    @XmlAttribute(name = NAME)
    public String name = null;

    /**
     * Text for the label.
     */
    @XmlAttribute(name = TEXT)
    public String text = null;

    /**
     * The widget order.
     */
    @XmlAttribute(name = ORDER)
    public Integer order = null;

    /**
     * The layout constraints.
     */
    @XmlAttribute(name = CONSTRAINTS)
    public String constraints = null;

    @Override
    public int getOrder() {
        if (order == null) {
            order = 0;
        }
        return order;
    }

    @Override
    public String getName() {
        return name;
    }
}
