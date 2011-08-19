/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
�* All rights reserved. This program and the accompanying materials
�* are made available under the terms of the Eclipse Public License v1.0
�* which accompanies this distribution, and is available at
�* http://www.eclipse.org/legal/epl-v10.html
�*
�* Contributors:
�*����Elias Volanakis - initial API and implementation
�*******************************************************************************/
package eu.hydrologis.jgrass.formeditor.model;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.type.AttributeDescriptor;

/**
 * A container for multiple shapes.
 * This is the "root" of the model data structure.
 * @author Elias Volanakis
 */
public class WidgetsDiagram extends AModelElement {

    /** Property ID to use when a child is added to this diagram. */
    public static final String CHILD_ADDED_PROP = "WidgetsDiagram.ChildAdded";
    /** Property ID to use when a child is removed from this diagram. */
    public static final String CHILD_REMOVED_PROP = "WidgetsDiagram.ChildRemoved";
    private static final long serialVersionUID = 1;
    private List<AWidget> widgetsList = new ArrayList<AWidget>();
    private List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();

    public void setAttributeDescriptors( List<AttributeDescriptor> attributeDescriptors ) {
        this.attributeDescriptors = attributeDescriptors;
    }

    public List<AttributeDescriptor> getAttributeDescriptors() {
        return attributeDescriptors;
    }

    /** 
     * Add a shape to this diagram.
     * @param widget a non-null shape instance
     * @return true, if the shape was added, false otherwise
     */
    public boolean addChild( AWidget widget ) {
        if (widget != null && widgetsList.add(widget)) {
            firePropertyChange(CHILD_ADDED_PROP, null, widget);
            return true;
        }
        return false;
    }

    /** Return a List of Shapes in this diagram.  The returned List should not be modified. */
    public List<AWidget> getChildren() {
        return widgetsList;
    }

    /**
     * Remove a shape from this diagram.
     * @param widget a non-null shape instance;
     * @return true, if the shape was removed, false otherwise
     */
    public boolean removeChild( AWidget widget ) {
        if (widget != null && widgetsList.remove(widget)) {
            firePropertyChange(CHILD_REMOVED_PROP, null, widget);
            return true;
        }
        return false;
    }
}