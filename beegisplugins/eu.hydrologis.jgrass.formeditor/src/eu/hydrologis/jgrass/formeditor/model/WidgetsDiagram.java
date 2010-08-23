/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *******************************************************************************/
package eu.hydrologis.jgrass.formeditor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for multiple shapes.
 * This is the "root" of the model data structure.
 * @author Elias Volanakis
 */
public class WidgetsDiagram extends ModelElement {

    /** Property ID to use when a child is added to this diagram. */
    public static final String CHILD_ADDED_PROP = "WidgetsDiagram.ChildAdded";
    /** Property ID to use when a child is removed from this diagram. */
    public static final String CHILD_REMOVED_PROP = "WidgetsDiagram.ChildRemoved";
    private static final long serialVersionUID = 1;
    private List<Widget> widgetsList = new ArrayList<Widget>();

    /** 
     * Add a shape to this diagram.
     * @param widget a non-null shape instance
     * @return true, if the shape was added, false otherwise
     */
    public boolean addChild( Widget widget ) {
        if (widget != null && widgetsList.add(widget)) {
            firePropertyChange(CHILD_ADDED_PROP, null, widget);
            return true;
        }
        return false;
    }

    /** Return a List of Shapes in this diagram.  The returned List should not be modified. */
    public List<Widget> getChildren() {
        return widgetsList;
    }

    /**
     * Remove a shape from this diagram.
     * @param widget a non-null shape instance;
     * @return true, if the shape was removed, false otherwise
     */
    public boolean removeChild( Widget widget ) {
        if (widget != null && widgetsList.remove(widget)) {
            firePropertyChange(CHILD_REMOVED_PROP, null, widget);
            return true;
        }
        return false;
    }
}