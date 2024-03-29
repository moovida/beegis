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
package eu.hydrologis.jgrass.formeditor.model.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import eu.hydrologis.jgrass.formeditor.model.AWidget;

/**
 * A command to resize and/or move a shape.
 * The command can be undone or redone.
 * @author Elias Volanakis
 */
public class WidgetSetConstraintCommand extends Command {
    /** Stores the new size and location. */
    private final Rectangle newBounds;
    /** Stores the old size and location. */
    private Rectangle oldBounds;
    /** A request to move/resize an edit part. */
    private final ChangeBoundsRequest request;

    /** Shape to manipulate. */
    private final AWidget widget;

    /**
     * Create a command that can resize and/or move a shape. 
     * @param widget	the shape to manipulate
     * @param req		the move and resize request
     * @param newBounds the new size and location
     * @throws IllegalArgumentException if any of the parameters is null
     */
    public WidgetSetConstraintCommand( AWidget widget, ChangeBoundsRequest req, Rectangle newBounds ) {
        if (widget == null || req == null || newBounds == null) {
            throw new IllegalArgumentException();
        }
        this.widget = widget;
        this.request = req;
        this.newBounds = newBounds.getCopy();
        setLabel("move / resize");
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        Object type = request.getType();
        // make sure the Request is of a type we support:
        return (RequestConstants.REQ_MOVE.equals(type)
                || RequestConstants.REQ_MOVE_CHILDREN.equals(type)
                || RequestConstants.REQ_RESIZE.equals(type) || RequestConstants.REQ_RESIZE_CHILDREN
                .equals(type));
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        oldBounds = new Rectangle(widget.getLocation(), widget.getSize());
        redo();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        widget.setSize(newBounds.getSize());
        widget.setLocation(newBounds.getLocation());
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        widget.setSize(oldBounds.getSize());
        widget.setLocation(oldBounds.getLocation());
    }
}
