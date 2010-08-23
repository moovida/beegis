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
package eu.hydrologis.jgrass.formeditor.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import eu.hydrologis.jgrass.formeditor.model.Widget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;

/**
 * Factory that maps model elements to TreeEditParts.
 * TreeEditParts are used in the outline view of the ShapesEditor.
 * @author Elias Volanakis
 */
public class WidgetsTreeEditPartFactory implements EditPartFactory {

    /* (non-Javadoc)
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    public EditPart createEditPart( EditPart context, Object model ) {
        if (model instanceof Widget) {
            return new WidgetsTreeEditPart((Widget) model);
        }
        if (model instanceof WidgetsDiagram) {
            return new WidgetDiagramTreeEditPart((WidgetsDiagram) model);
        }
        return null; // will not show an entry for the corresponding model instance
    }

}
