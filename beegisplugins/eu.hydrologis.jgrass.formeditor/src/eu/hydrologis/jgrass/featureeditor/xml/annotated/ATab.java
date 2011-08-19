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

import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.COL;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.LAYOUT;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.NAME;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.ORDER;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.ROW;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.TAB;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.TEXT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.hydrologis.jgrass.formeditor.utils.CellConstraint;

/**
 * A class representing the main tabbed component.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@XmlRootElement(name = TAB)
public class ATab extends FormElement {

    @XmlAttribute(name = NAME)
    public String name = null;

    @XmlAttribute(name = TEXT)
    public String text = null;

    @XmlAttribute(name = LAYOUT)
    public String layoutConstraints;

    @XmlAttribute(name = COL)
    public String colConstraints;

    @XmlAttribute(name = ROW)
    public String rowConstraints;

    @XmlElement
    public List<ACheckBox> checkbox = new ArrayList<ACheckBox>();

    @XmlElement
    public List<AComboBox> combobox = new ArrayList<AComboBox>();

    @XmlElement
    public List<ALabel> label = new ArrayList<ALabel>();

    @XmlElement
    public List<ASeparator> separator = new ArrayList<ASeparator>();

    @XmlElement
    public List<ARadioButton> radiobutton = new ArrayList<ARadioButton>();

    @XmlElement
    public List<ATextArea> textarea = new ArrayList<ATextArea>();

    @XmlElement
    public List<ATextField> textfield = new ArrayList<ATextField>();

    public List< ? extends FormElement> getOrderedElements() {
        List<FormElement> orderedElements = new ArrayList<FormElement>();
        orderedElements.addAll(checkbox);
        orderedElements.addAll(combobox);
        orderedElements.addAll(label);
        orderedElements.addAll(separator);
        orderedElements.addAll(radiobutton);
        orderedElements.addAll(textarea);
        orderedElements.addAll(textfield);
        Collections.sort(orderedElements);

        return orderedElements;
    }

    /**
     * The widget order.
     */
    @XmlAttribute(name = ORDER)
    public Float order = null;

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
        return null;
    }

    public void setConstraints( String constraints ) {
    }

    public void closeConstraints() {
    }

    public int compareTo( FormElement o ) {
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
