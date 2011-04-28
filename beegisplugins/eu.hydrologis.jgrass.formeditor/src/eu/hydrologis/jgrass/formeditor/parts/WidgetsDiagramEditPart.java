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
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
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

import eu.hydrologis.jgrass.formeditor.model.AModelElement;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;
import eu.hydrologis.jgrass.formeditor.model.commands.WidgetCreateCommand;
import eu.hydrologis.jgrass.formeditor.model.commands.WidgetSetConstraintCommand;
import eu.hydrologis.jgrass.formeditor.model.widgets.CheckBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.ComboBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.RadioButtonWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.SeparatorWidget;
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

    protected void createEditPolicies() {
        // disallows the removal of this edit part from its parent
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        // handles constraint changes (e.g. moving and/or resizing) of model elements
        // and creation of new model elements
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ShapesXYLayoutEditPolicy(this));
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
    }

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

        private final WidgetsDiagramEditPart widgetsDiagramEditPart;

        public ShapesXYLayoutEditPolicy( WidgetsDiagramEditPart widgetsDiagramEditPart ) {
            this.widgetsDiagramEditPart = widgetsDiagramEditPart;
        }

        protected Command createChangeConstraintCommand( ChangeBoundsRequest request, EditPart child, Object constraint ) {
            if (child instanceof WidgetEditPart && constraint instanceof Rectangle) {
                // check overlap
                Rectangle r = (Rectangle) constraint;
                AWidget currentWidget = (AWidget) child.getModel();
                r = currentWidget.snapToBounds(r);
                List<AWidget> otherWidgets = widgetsDiagramEditPart.getModelChildren();
                for( AWidget aWidget : otherWidgets ) {
                    if (currentWidget.equals(aWidget)) {
                        continue;
                    }
                    Rectangle bounds = aWidget.getBounds();
                    if (bounds.intersects(r)) {
                        return null;
                    }
                }

                // return a command that can move and/or resize a Shape
                return new WidgetSetConstraintCommand(currentWidget, request, r);
            }
            return super.createChangeConstraintCommand(request, child, constraint);
        }

        protected Command createChangeConstraintCommand( EditPart child, Object constraint ) {
            // not used in this example
            return null;
        }

        protected Command getCreateCommand( CreateRequest request ) {
            Object childClass = request.getNewObjectType();

            Point location = request.getLocation();
            Dimension size = Constants.DEFAULT_DIMENSION;
            Rectangle r = new Rectangle(location, size);
            AWidget currentWidget = (AWidget) request.getNewObject();
            r = currentWidget.snapToBounds(r);

            if (childClass == TextFieldWidget.class || childClass == TextAreaWidget.class || childClass == LabelWidget.class
                    || childClass == SeparatorWidget.class || childClass == ComboBoxWidget.class
                    || childClass == CheckBoxWidget.class || childClass == RadioButtonWidget.class) {
                List<AWidget> otherWidgets = widgetsDiagramEditPart.getModelChildren();
                // check overlap
                for( AWidget aWidget : otherWidgets ) {
                    Rectangle bounds = aWidget.getBounds();
                    if (bounds.intersects(r)) {
                        return null;
                    }
                }

                WidgetsDiagram widgetsDiagram = (WidgetsDiagram) getHost().getModel();
                // return a command that can add a Shape to a ShapesDiagram
                return new WidgetCreateCommand(currentWidget, widgetsDiagram, (Rectangle) getConstraintFor(request));
            }
            return null;
        }

    }

}