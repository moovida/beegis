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

/**
 * A class representing the main tabbed component.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@XmlRootElement(name = TAB)
public class Tab extends OrderedElement {

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
    public List<CheckBox> checkbox = new ArrayList<CheckBox>();

    @XmlElement
    public List<ComboBox> combobox = new ArrayList<ComboBox>();

    @XmlElement
    public List<Label> label = new ArrayList<Label>();

    @XmlElement
    public List<RadioButton> radiobutton = new ArrayList<RadioButton>();

    @XmlElement
    public List<TextArea> textarea = new ArrayList<TextArea>();

    @XmlElement
    public List<TextField> textfield = new ArrayList<TextField>();

    public List< ? extends OrderedElement> getOrderedElements() {
        List<OrderedElement> orderedElements = new ArrayList<OrderedElement>();
        orderedElements.addAll(checkbox);
        orderedElements.addAll(combobox);
        orderedElements.addAll(label);
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
    public Integer order = null;

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
