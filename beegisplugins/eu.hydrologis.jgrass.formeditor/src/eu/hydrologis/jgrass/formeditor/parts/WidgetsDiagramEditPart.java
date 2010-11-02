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
package eu.hydrologis.jgrass.formeditor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.CoordinateListener;
import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.FocusListener;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IClippingStrategy;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;

import eu.hydrologis.jgrass.formeditor.model.AModelElement;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;
import eu.hydrologis.jgrass.formeditor.model.commands.WidgetCreateCommand;
import eu.hydrologis.jgrass.formeditor.model.commands.WidgetSetConstraintCommand;
import eu.hydrologis.jgrass.formeditor.model.widgets.CheckBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.ComboBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.DoubleFieldWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.IntegerFieldWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.RadioButtonWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextAreaWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;
import eu.hydrologis.jgrass.formeditor.utils.Constants;

/**
 * EditPart for the a ShapesDiagram instance.
 * <p>This edit part server as the main diagram container, the white area where
 * everything else is in. Also responsible for the container's layout (the
 * way the container rearanges is contents) and the container's capabilities
 * (edit policies).
 * </p>
 * <p>This edit part must implement the PropertyChangeListener interface, 
 * so it can be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 */
class WidgetsDiagramEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    /**
     * Upon activation, attach to the model element as a property change listener.
     */
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((AModelElement) getModel()).addPropertyChangeListener(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {
        // disallows the removal of this edit part from its parent
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        // handles constraint changes (e.g. moving and/or resizing) of model elements
        // and creation of new model elements
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ShapesXYLayoutEditPolicy());
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        GridLayer f = new GridLayer();
        f.setBorder(new MarginBorder(3));
        f.setLayoutManager(new FreeformLayout());
        // Create the static router for the connection layer
        ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));

        f.setSpacing(new Dimension(Constants.DIMENSION_PIXEL_SNAP, Constants.DIMENSION_PIXEL_SNAP));
        return f;
    }

    /**
     * Upon deactivation, detach from the model element as a property change listener.
     */
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((AModelElement) getModel()).removePropertyChangeListener(this);
        }
    }

    private WidgetsDiagram getCastedModel() {
        return (WidgetsDiagram) getModel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    protected List<AWidget> getModelChildren() {
        return getCastedModel().getChildren(); // return a list of shapes
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange( PropertyChangeEvent evt ) {
        String prop = evt.getPropertyName();
        // these properties are fired when Shapes are added into or removed from
        // the ShapeDiagram instance and must cause a call of refreshChildren()
        // to update the diagram's contents.
        if (WidgetsDiagram.CHILD_ADDED_PROP.equals(prop) || WidgetsDiagram.CHILD_REMOVED_PROP.equals(prop)) {
            refreshChildren();
        }
    }

    /**
     * EditPolicy for the Figure used by this edit part.
     * Children of XYLayoutEditPolicy can be used in Figures with XYLayout.
     * @author Elias Volanakis
     */
    private static class ShapesXYLayoutEditPolicy extends XYLayoutEditPolicy {

        /* (non-Javadoc)
         * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(ChangeBoundsRequest, EditPart, Object)
         */
        protected Command createChangeConstraintCommand( ChangeBoundsRequest request, EditPart child, Object constraint ) {
            if (child instanceof WidgetEditPart && constraint instanceof Rectangle) {
                // return a command that can move and/or resize a Shape
                return new WidgetSetConstraintCommand((AWidget) child.getModel(), request, (Rectangle) constraint);
            }
            return super.createChangeConstraintCommand(request, child, constraint);
        }

        /* (non-Javadoc)
         * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(EditPart, Object)
         */
        protected Command createChangeConstraintCommand( EditPart child, Object constraint ) {
            // not used in this example
            return null;
        }

        /* (non-Javadoc)
         * @see LayoutEditPolicy#getCreateCommand(CreateRequest)
         */
        protected Command getCreateCommand( CreateRequest request ) {
            Object childClass = request.getNewObjectType();
            if (childClass == TextFieldWidget.class || childClass == TextAreaWidget.class || childClass == LabelWidget.class
                    || childClass == IntegerFieldWidget.class || childClass == DoubleFieldWidget.class
                    || childClass == ComboBoxWidget.class || childClass == CheckBoxWidget.class
                    || childClass == RadioButtonWidget.class) {
                // return a command that can add a Shape to a ShapesDiagram
                return new WidgetCreateCommand((AWidget) request.getNewObject(), (WidgetsDiagram) getHost().getModel(),
                        (Rectangle) getConstraintFor(request));
            }
            return null;
        }

    }

}