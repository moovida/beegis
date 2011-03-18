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

import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.FORM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The wrapping form class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@XmlRootElement(name = FORM)
public class AForm extends FormElement {
    @XmlElement
    public List<ATab> tab = new ArrayList<ATab>();

    public List<ATab> getOrderedTabs() {
        Collections.sort(tab);
        return tab;
    }

    @Override
    public String getName() {
        return "ROOT"; //$NON-NLS-1$
    }

    @Override
    public float getOrder() {
        return -1;
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
}
